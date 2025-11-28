package sad.storereg.services.appdata;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ActionDTO;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.dto.appdata.PublishedWaitingListWithEntriesDTO;
import sad.storereg.dto.appdata.WaitingListEntryDTO;
import sad.storereg.dto.appdata.WaitingListsDTO;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsHistory;
import sad.storereg.models.appdata.ApplicationsWaitingList;
import sad.storereg.models.appdata.PublishedWaitingList;
import sad.storereg.models.appdata.PublishedWaitingListEntry;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationFlow;
import sad.storereg.models.master.ApplicationStatus;
import sad.storereg.models.master.Quarters;
import sad.storereg.models.master.WaitingList;
import sad.storereg.repo.appdata.AllotmentsRepository;
import sad.storereg.repo.appdata.ApplicationHistoryRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.appdata.ApplicationsWaitingListRepository;
import sad.storereg.repo.master.ApplicationFlowRepository;
import sad.storereg.repo.master.ApplicationStatusRepository;
import sad.storereg.repo.master.QuartersRepository;
import sad.storereg.repo.master.WaitingListRepository;

@Service
@RequiredArgsConstructor
public class WaitingListServices {
	
	private final ApplicationsRepository appRepo;
	private final WaitingListRepository waitingListRepo;
	private final ApplicationFlowRepository appFlowRepo;
	private final ApplicationHistoryRepository appHistoryRepo;
	private final ApplicationsWaitingListRepository appWaitingListRepo;
	private final ApplicationStatusRepository appStatusRepo;
	private final AllotmentsRepository allotmentsRepo;
	private final QuartersRepository quartersRepo;
	private final FormServices formService;	
	
	public List<WaitingList> getList(){
		try {
			return waitingListRepo.findAll();
		}
		catch(Exception ex) {
			throw ex;
		}
	}

	@Transactional
	public ResponseEntity<Map<String, String>> moveToWL(ActionRequest request, User user) throws Exception {
		Map<String, String> responseMap = new HashMap<>();
		try {
			Applications application = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(application.getAppStatus(), request.getActionCode());			
			
			ApplicationsHistory move = ApplicationsHistory.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate(new Date()).userCode(user.getId())
					.flowCode(appFlow.getCode()).build();
			appHistoryRepo.save(move);			
			
//			if(application.getAppStatus()==11) { //When CS send back to CH for change - might not need anymore
//				//Re-organise the waiting list itself
//				ApplicationsWaitingList appWL = getWaitingList(application.getAppNo());
//				deleteWaitingList(appWL);
//				insertInWL(request.getWaitingList(), request.getWlSlNo(), application.getAppNo(), new Date(), request.getRemarks());
//				
//				Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Invalid Allotment ID"));
//				Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid quarter no."));
//				quarter.setInAllotmentList(0);
//				quartersRepo.save(quarter);
//				
//			}
//			else 
			if(application.getAppStatus()==10 ) {//Applicant request for change
				Map<String, Object> data = getPreviousWaitingListData(application.getAppNo());
				ApplicationsWaitingList list = ApplicationsWaitingList.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate((java.sql.Timestamp) data.get("prevWlDate")).waitingListCode(request.getWaitingList()).waitingListNo(0).build();//.waitingListNo(getSlNo(request.getWaitingList())).build();
				appWaitingListRepo.save(list);
				
				Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Invalid Allotment ID"));
				Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid quarter no."));
				quarter.setInAllotmentList(0);
				quarter.setQuarterStatus(0);
				quartersRepo.save(quarter);
			}
			else if(application.getAppStatus()==1 || application.getAppStatus()==11) {
				ApplicationsWaitingList appWaitingList = ApplicationsWaitingList.builder().appNo(request.getAppNo()).entrydate(new Date()).remarks(request.getRemarks()).waitingListCode(request.getWaitingList()).waitingListNo(0).build();//.waitingListNo(getSlNo(request.getWaitingList())).build();
			    appWaitingListRepo.save(appWaitingList);
			}
			
			//else {
			//	ApplicationsWaitingList list = ApplicationsWaitingList.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate(new Date()).waitingListCode(request.getWaitingList()).waitingListNo(getSlNo(request.getWaitingList())).build();
			//	appWaitingListRepo.save(list);
			//}
			
			application.setLevel(appFlow.getNextLevel());
			application.setAppStatus(request.getActionCode());

			appRepo.save(application);
			reassignDraftWaitingListNumbers(request.getWaitingList());
			
			responseMap.put("detail", "Application Moved");
			
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	private ResponseEntity<Map<String, String>> move(ActionRequest request, Applications application) {
		Map<String, String> responseMap = new HashMap<>();
		if(application.getAppStatus()==2) {
			ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Waiting List entry not found"));
			
			if(appWaitingList.getWaitingListCode()==request.getWaitingList())
				throw new UnauthorizedException("Cannot perform action. Waiting List should be different");
			
			appWaitingList.setWaitingListCode(request.getWaitingList());
			appWaitingListRepo.save(appWaitingList);
			
			reassignDraftWaitingListNumbers(appWaitingList.getWaitingListCode());
			
			application.setWlCode(appWaitingList.getWaitingListCode());
			application.setWlSlNo(appWaitingList.getWaitingListNo());
			application.setWaitingListName(getWaitingListName(appWaitingList.getWaitingListCode()));
			appRepo.save(application);
			responseMap.put("detail", "Application Moved");
			
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
		else return null;
			
	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> moveToApprovedWL(ActionRequest request, User user) throws Exception {
		Map<String, String> responseMap = new HashMap<>();
		try {
			Applications application = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			if(application.getAppStatus()==2 && request.getActionCode()==12) {
				return move(request, application);
			}
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(application.getAppStatus(), request.getActionCode());			
			
			ApplicationsHistory move = ApplicationsHistory.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate(new Date()).userCode(user.getId())
					.flowCode(appFlow.getCode()).build();
			appHistoryRepo.save(move);			
			
			if(application.getAppStatus()==10 ) {//Applicant request for change
				Map<String, Object> data = getPreviousWaitingListData(application.getAppNo());
				ApplicationsWaitingList list = ApplicationsWaitingList.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate((java.sql.Timestamp) data.get("prevWlDate")).waitingListCode(request.getWaitingList()).waitingListNo(0).build();//.waitingListNo(request.getWlSlNo()).build();
				appWaitingListRepo.save(list);
				reassignApprovedWaitingListNumbers(list.getWaitingListCode());
				
				Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Invalid Allotment ID"));
				Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid quarter no."));
				quarter.setInAllotmentList(0);
				quarter.setQuarterStatus(0);
				quartersRepo.save(quarter);
			}
			else if(application.getAppStatus()==2) {
				ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Draft Waiting List entry not found"));
				appWaitingList.setIsApproved(1);
				appWaitingList.setLetterNo(request.getLetterNo());
				appWaitingList.setDocCode(request.getDocCode());
				appWaitingList.setApprovedTimestamp(new Date());;
				appWaitingListRepo.save(appWaitingList);
				reassignApprovedWaitingListNumbers(appWaitingList.getWaitingListCode());
				
				application.setWlCode(appWaitingList.getWaitingListCode());
				application.setWlSlNo(appWaitingList.getWaitingListNo());
				application.setWaitingListName(getWaitingListName(appWaitingList.getWaitingListCode()));
			}
			//else {
			//	ApplicationsWaitingList list = ApplicationsWaitingList.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate(new Date()).waitingListCode(request.getWaitingList()).waitingListNo(getSlNo(request.getWaitingList())).build();
			//	appWaitingListRepo.save(list);
			//}
			
			application.setLevel(appFlow.getNextLevel());
			application.setAppStatus(request.getActionCode());
			
			//See later
			if(request.getActionCode()==12) { //Move to same/different waiting-list				
				
				ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Waiting List entry not found"));
				
				if(appWaitingList.getWaitingListCode()==request.getWaitingList())
					throw new UnauthorizedException("Cannot perform action. Waiting List should be different");
				
				appWaitingList.setIsApproved(1);
				//Question should i save these old docs
				appWaitingList.setLetterNo(request.getLetterNo());
				appWaitingList.setDocCode(request.getDocCode());
				appWaitingList.setApprovedTimestamp(new Date());
				appWaitingList.setWaitingListCode(request.getWaitingList());
				appWaitingListRepo.save(appWaitingList);
				
				//insertInWL(request.getWaitingList(),request.getWlSlNo(), application.getAppNo(), new Date(), request.getRemarks());
				if(appWaitingList.getIsApproved()==1)
					reassignApprovedWaitingListNumbers(appWaitingList.getWaitingListCode());
				else
					reassignDraftWaitingListNumbers(appWaitingList.getWaitingListCode());
				
				application.setWlCode(appWaitingList.getWaitingListCode());
				application.setWlSlNo(appWaitingList.getWaitingListNo());
				application.setWaitingListName(getWaitingListName(appWaitingList.getWaitingListCode()));
				
				application.setAppStatus(3);
			}
			appRepo.save(application);
			
			responseMap.put("detail", "Application Moved");
			
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
		catch(Exception ex) {
			throw ex;
		}
	}	
	
	public List<WaitingListsDTO> getApplicationsWaitingList() {
		try {
			List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findAll();
			return waitingList.stream().map(application -> {
	            WaitingListsDTO dto = new WaitingListsDTO();
				dto.setAppNo(application.getAppNo());
	            dto.setEntrydate(application.getEntrydate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
	            dto.setWaitingListCode(application.getWaitingListCode());
	            dto.setWaitingListNo(application.getWaitingListNo());
	            
	            Applications app = appRepo.findByAppNo(application.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
	            
	            dto.setName(app.getName());
	            dto.setDesignation(app.getDesignation());
	            dto.setDepartment(app.getDepartmentOrDirectorate()+", "+app.getOfficeAddress());
	            dto.setScaleOfPay(app.getScaleOfPay());
	            dto.setDateOfRetirement(app.getDateOfRetirement());
	            dto.setBasicPay(app.getBasicPay());
	            dto.setDepartmentOrDirectorate(app.getDepartmentOrDirectorate());
	            dto.setOfficeAddress(app.getOfficeAddress());
	            dto.setOfficeTelephone(app.getOfficeTelephone());
	            dto.setMaritalStatus(app.getMaritalStatus());
	            dto.setEmploymentStatus(app.getEmploymentStatus());
	            dto.setSpouseAccommodation(app.getSpouseAccommodation());
	            dto.setAccommdationDetails(app.getAccommdationDetails());
	            dto.setService(app.getService());
	            dto.setOtherServicesDetails(app.getOtherServicesDetails());
	            dto.setCentralDeputation(app.getCentralDeputation());
	            dto.setDeputationPeriod(app.getDeputationPeriod());
	            dto.setDebarred(app.getDebarred());
	            dto.setDebarredUptoDate(app.getDebarredUptoDate());
	            dto.setOwnHouse(app.getOwnHouse());
	            dto.setParticularsOfHouse(app.getParticularsOfHouse());
	            dto.setHouseBuildingAdvance(app.getHouseBuildingAdvance());
	            dto.setLoanYear(app.getLoanYear());
	            dto.setHouseConstructed(app.getHouseConstructed());
	            dto.setHouseLocation(app.getHouseLocation());
	            dto.setPresentAddress(app.getPresentAddress());
	            dto.setDeptHasQuarter(app.getDeptHasQuarter());
	            dto.setReasonDeptQuarter(app.getReasonDeptQuarter());
	            dto.setUploadTimestamp(app.getUploadTimestamp());
	            
	            List<ActionDTO> actions = new ArrayList<>();
	            List<ApplicationFlow> allowedFlows = appFlowRepo.findAllByFromStatus(app.getAppStatus());
	            
	            if(app.getAppStatus()==4) {
                	actions.add(new ActionDTO("Pending with CS for approval", 0, -1));
	            }
	            if(app.getAppStatus()==5) {
                	actions.add(new ActionDTO("Pending with you for final approval", 0, -1));
	            }
	            if(app.getAppStatus()==11) {
                	actions.add(new ActionDTO("CS returned application. \n Pending for action", 0, -1));
	            }
	            if(app.getAppStatus()==3) {
                	actions.add(new ActionDTO("Allot", 1, 4));
	            }
	            if(app.getAppStatus()==22 || app.getAppStatus()==23)
	            	actions.add(new ActionDTO("Pending for approval in e-Proposal",0,-1));
	            
//	            	for (ApplicationFlow flow : allowedFlows) {
//	            		System.out.println("Flow: "+flow);
//	                    //ApplicationLevels level2 = appLevelRepo.findByRole(userRole);
//	                    ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());
//	                    System.out.println("Status: "+status);		
//	                    //Allowed to Cancel, enabled Cancel Button
//	                    if(app.getAppStatus()==3)
//	                    	actions.add(new ActionDTO(status.getAction(), 1, status.getStatusCode()));
//	                    else if(app.getAppStatus()==4) {
//	                    	actions.add(new ActionDTO("Pending with CS for approval", 0, status.getStatusCode()));
//	                    	System.out.println("In here: "+actions);
//	                }}
	            dto.setActions(actions);
	            return dto;
	        }).collect(Collectors.toList());
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public List<WaitingListsDTO> getApplicationsWaitingListByCode(Integer wlCode) {
		try {
			//List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findAllByWaitingListCodeOrderByWaitingListNoAsc(wlCode);
			//List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findAllByWaitingListCodeAndIsApprovedIsNullOrderByWaitingListNoAsc(wlCode);
			List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findByWaitingListCodeAndIsApprovedNullOrZero(wlCode);
			return waitingList.stream().map(application -> {
	            WaitingListsDTO dto = new WaitingListsDTO();
				dto.setAppNo(application.getAppNo());
	            dto.setEntrydate(application.getEntrydate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
	            dto.setWaitingListCode(application.getWaitingListCode());
	            dto.setWaitingListNo(application.getWaitingListNo());
	            
	            Applications app = appRepo.findByAppNo(application.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
	            
	            dto.setName(app.getName());
	            dto.setDesignation(app.getDesignation());
	            dto.setDepartment(app.getDepartmentOrDirectorate()+", "+app.getOfficeAddress());
	            dto.setScaleOfPay(app.getScaleOfPay());
	            dto.setDateOfRetirement(app.getDateOfRetirement());
	            dto.setBasicPay(app.getBasicPay());
	            dto.setDepartmentOrDirectorate(app.getDepartmentOrDirectorate());
	            dto.setOfficeAddress(app.getOfficeAddress());
	            dto.setOfficeTelephone(app.getOfficeTelephone());
	            dto.setMaritalStatus(app.getMaritalStatus());
	            dto.setEmploymentStatus(app.getEmploymentStatus());
	            dto.setSpouseAccommodation(app.getSpouseAccommodation());
	            dto.setAccommdationDetails(app.getAccommdationDetails());
	            dto.setService(app.getService());
	            dto.setOtherServicesDetails(app.getOtherServicesDetails());
	            dto.setCentralDeputation(app.getCentralDeputation());
	            dto.setDeputationPeriod(app.getDeputationPeriod());
	            dto.setDebarred(app.getDebarred());
	            dto.setDebarredUptoDate(app.getDebarredUptoDate());
	            dto.setOwnHouse(app.getOwnHouse());
	            dto.setParticularsOfHouse(app.getParticularsOfHouse());
	            dto.setHouseBuildingAdvance(app.getHouseBuildingAdvance());
	            dto.setLoanYear(app.getLoanYear());
	            dto.setHouseConstructed(app.getHouseConstructed());
	            dto.setHouseLocation(app.getHouseLocation());
	            dto.setPresentAddress(app.getPresentAddress());
	            dto.setDeptHasQuarter(app.getDeptHasQuarter());
	            dto.setReasonDeptQuarter(app.getReasonDeptQuarter());
	            dto.setUploadTimestamp(app.getUploadTimestamp());
	            
	            List<ActionDTO> actions = new ArrayList<>();
	            List<ApplicationFlow> allowedFlows = appFlowRepo.findAllByFromStatus(app.getAppStatus());
	            
	            for (ApplicationFlow flow : allowedFlows) {
                    ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());	
                    //Allowed to Cancel, enabled Cancel Button
                    if(app.getAppStatus()==2 && flow.getToStatus()!=7)
                    	actions.add(new ActionDTO(status.getAction(), 1, status.getStatusCode()));
                    }
	            if(app.getAppStatus()==4) {
                	actions.add(new ActionDTO("Pending with CS for approval", 0, -1));
	            }
	            if(app.getAppStatus()==5) {
                	actions.add(new ActionDTO("Pending with you for final approval", 0, -1));
	            }
	            if(app.getAppStatus()==11) {
                	actions.add(new ActionDTO("CS returned application. \n Pending for action", 0, -1));
	            }
	            if(app.getAppStatus()==22 || app.getAppStatus()==23)
	            	actions.add(new ActionDTO("Pending for approval in e-Proposal",0,-1));
	            dto.setActions(actions);
	            
	            return dto;
			 }).collect(Collectors.toList());
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public List<WaitingListsDTO> getApprovedWaitingListByCode(Integer wlCode) {
		try {
			List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findAllByWaitingListCodeAndIsApprovedEqualsOrderByWaitingListNoAsc(wlCode, 1);
			return waitingList.stream().map(application -> {
	            WaitingListsDTO dto = new WaitingListsDTO();
				dto.setAppNo(application.getAppNo());
	            dto.setEntrydate(application.getEntrydate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
	            dto.setWaitingListCode(application.getWaitingListCode());
	            dto.setWaitingListNo(application.getWaitingListNo());
	            dto.setLetterNo(application.getLetterNo());
	            dto.setDocCode(application.getDocCode());
	            dto.setApprovalTimestamp(application.getApprovedTimestamp());
	            
	            Applications app = appRepo.findByAppNo(application.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
	            
	            dto.setName(app.getName());
	            dto.setDesignation(app.getDesignation());
	            dto.setDepartment(app.getDepartmentOrDirectorate()+", "+app.getOfficeAddress());
	            dto.setScaleOfPay(app.getScaleOfPay());
	            dto.setDateOfRetirement(app.getDateOfRetirement());
	            dto.setBasicPay(app.getBasicPay());
	            dto.setDepartmentOrDirectorate(app.getDepartmentOrDirectorate());
	            dto.setOfficeAddress(app.getOfficeAddress());
	            dto.setOfficeTelephone(app.getOfficeTelephone());
	            dto.setMaritalStatus(app.getMaritalStatus());
	            dto.setEmploymentStatus(app.getEmploymentStatus());
	            dto.setSpouseAccommodation(app.getSpouseAccommodation());
	            dto.setAccommdationDetails(app.getAccommdationDetails());
	            dto.setService(app.getService());
	            dto.setOtherServicesDetails(app.getOtherServicesDetails());
	            dto.setCentralDeputation(app.getCentralDeputation());
	            dto.setDeputationPeriod(app.getDeputationPeriod());
	            dto.setDebarred(app.getDebarred());
	            dto.setDebarredUptoDate(app.getDebarredUptoDate());
	            dto.setOwnHouse(app.getOwnHouse());
	            dto.setParticularsOfHouse(app.getParticularsOfHouse());
	            dto.setHouseBuildingAdvance(app.getHouseBuildingAdvance());
	            dto.setLoanYear(app.getLoanYear());
	            dto.setHouseConstructed(app.getHouseConstructed());
	            dto.setHouseLocation(app.getHouseLocation());
	            dto.setPresentAddress(app.getPresentAddress());
	            dto.setDeptHasQuarter(app.getDeptHasQuarter());
	            dto.setReasonDeptQuarter(app.getReasonDeptQuarter());
	            dto.setUploadTimestamp(app.getUploadTimestamp());
	            
	            List<ActionDTO> actions = new ArrayList<>();
	            List<ApplicationFlow> allowedFlows = appFlowRepo.findAllByFromStatus(app.getAppStatus());
	            
	            for (ApplicationFlow flow : allowedFlows) {
                    ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());	
                    //Allowed to Cancel, enabled Cancel Button
                    if(app.getAppStatus()==3 && flow.getToStatus()!=7)
                    	actions.add(new ActionDTO(status.getAction(), 1, status.getStatusCode()));
                    }
	            if(app.getAppStatus()==4) {
                	actions.add(new ActionDTO("Pending with CS for approval", 0, -1));
	            }
	            if(app.getAppStatus()==5) {
                	actions.add(new ActionDTO("Pending with you for final approval", 0, -1));
	            }
	            if(app.getAppStatus()==11) {
                	actions.add(new ActionDTO("CS returned application. \n Pending for action", 0, -1));
	            }
	            if(app.getAppStatus()==22 || app.getAppStatus()==23)
	            	actions.add(new ActionDTO("Pending for approval in e-Proposal",0,-1));

	            if(app.getAppStatus()==3) {
	            	// Split out 4 and 23
	                List<ActionDTO> allotSubActions = actions.stream()
	                    .filter(a -> a.getActionCode() == 4 || a.getActionCode() == 23)
	                    .map(a -> {
	                        // Rename actionCode 4
	                        if (a.getActionCode() == 4) {
	                            return new ActionDTO(
	                                "Manually generate/upload allotment order",
	                                a.getIsEnabled(),
	                                a.getActionCode()
	                            );
	                        }
	                        return a;
	                    })
	                    .collect(Collectors.toList());

	                // Remove them from the main list
	                actions.removeIf(a -> a.getActionCode() == 4 || a.getActionCode() == 23);

	             // Create the combined "Allot" parent action
	                if (!allotSubActions.isEmpty()) {
	                    ActionDTO allotAction = new ActionDTO("Allot", 1, -100, allotSubActions); // -100 = placeholder code
	                    // Insert "Allot" at the beginning
	                    actions.add(0, allotAction);
	                }
	            }
	            
	         // Move statusCode == 6 to the end
	            actions.sort((a1, a2) -> {
	                if (a1.getActionCode() == 6) return 1;
	                if (a2.getActionCode() == 6) return -1;
	                return 0;
	            });
            dto.setActions(actions);
	            
	            return dto;
			 }).collect(Collectors.toList());
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	private Integer getSlNo(Integer wlCode) {
		Optional<ApplicationsWaitingList> el = appWaitingListRepo.findTopByWaitingListCodeOrderByWaitingListNoDesc(wlCode);
		if (el.isPresent()) {
		    ApplicationsWaitingList application = el.get();
		    return application.getWaitingListNo() + 1;
		} else {
		    return 1; // Default value if no record exists
		}
	}
	
	@Transactional
	public void reassignDraftWaitingListNumbers(Integer waitingListCode) {
	    List<ApplicationsWaitingList> waitingList = appWaitingListRepo.findByWaitingListCode(waitingListCode);

	    // Optional optimization: preload apps into a map (skip for now if not needed)
	    Function<ApplicationsWaitingList, LocalDateTime> getTimestamp = awl -> {
	        Applications app = appRepo.findByAppNo(awl.getAppNo()).orElse(null);
	        return app != null ? app.getUploadTimestamp() : LocalDateTime.MAX;
	    };

	    List<ApplicationsWaitingList> sorted = waitingList.stream()
	        .sorted(
	            Comparator
	                // Put unapproved (isApproved == null) first
	                .comparing((ApplicationsWaitingList awl) -> awl.getIsApproved() != null)
	                // Then sort by Applications.uploadTimestamp
	                .thenComparing(getTimestamp)
	        )
	        .collect(Collectors.toList());

	    int i = 1;
	    for (ApplicationsWaitingList awl : sorted) {
	        awl.setWaitingListNo(i++);
	    }

	    appWaitingListRepo.saveAll(sorted);
	}

	@Transactional
	public void reassignApprovedWaitingListNumbers(Integer waitingListCode) {
	    // Get only entries with isApproved == 1
	    List<ApplicationsWaitingList> approvedList = appWaitingListRepo
	        .findByWaitingListCodeAndIsApproved(waitingListCode, 1);

	    if (approvedList == null || approvedList.isEmpty()) {
	        return; // Nothing to process
	    }

	    Function<ApplicationsWaitingList, LocalDateTime> getTimestamp = awl -> {
	        Applications app = appRepo.findByAppNo(awl.getAppNo()).orElse(null);
	        return app != null ? app.getUploadTimestamp() : LocalDateTime.MAX;
	    };

	    List<ApplicationsWaitingList> sorted = approvedList.stream()
	        .sorted(Comparator.comparing(getTimestamp))
	        .collect(Collectors.toList());

	    int i = 1;
	    for (ApplicationsWaitingList awl : sorted) {
	        awl.setWaitingListNo(i++);
	    }

	    appWaitingListRepo.saveAll(sorted);
	}
	
	public ApplicationsWaitingList getWaitingList(String appNo) {
		try {
			return appWaitingListRepo.findByAppNo(appNo).orElseThrow();
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void deleteWaitingList(ApplicationsWaitingList appWL) {
		try {
			int wlCode = appWL.getWaitingListCode();
			int wlSlNo = appWL.getWaitingListNo();
			appWaitingListRepo.deleteById(appWL.getId());
			reOrganiseWaitingList(wlCode, wlSlNo);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void reOrganiseWaitingList(Integer waitingListCode, Integer waitingListNo) {
		// Fetch all applications with waitingListNo greater than the provided value
	    List<ApplicationsWaitingList> subsequentApplications = appWaitingListRepo
	        .findByWaitingListCodeAndWaitingListNoGreaterThanOrderByWaitingListNo(waitingListCode, waitingListNo);

	    // Update the waitingListNo of each application
	    for (ApplicationsWaitingList application : subsequentApplications) {
	        application.setWaitingListNo(application.getWaitingListNo() - 1);
	    }

	    // Save the updated applications back to the database
	    appWaitingListRepo.saveAll(subsequentApplications);
	}
	
	@Transactional
	public void insertInWL(Integer waitingListCode, Integer waitingListNo, String appNo, Date entrydate, String remarks) {
		try {
			// Fetch all applications with waitingListNo greater than the provided value
		    List<ApplicationsWaitingList> subsequentApplications = appWaitingListRepo
		        .findByWaitingListCodeAndWaitingListNoGreaterThanEqualOrderByWaitingListNo(waitingListCode, waitingListNo);

		    // Update the waitingListNo of each application
		    for (ApplicationsWaitingList application : subsequentApplications) {
		        application.setWaitingListNo(application.getWaitingListNo() + 1);
		    }
		    appWaitingListRepo.saveAll(subsequentApplications);

		    //ApplicationsWaitingList appWaitingList = ApplicationsWaitingList.builder().appNo(appNo).entrydate(entrydate).remarks(remarks).waitingListCode(waitingListCode).waitingListNo(waitingListNo).build();
		    ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Invalid application"));
		    int oldWLCode = appWaitingList.getWaitingListCode();
		    int oldWlSlNo = appWaitingList.getWaitingListNo();
		    appWaitingList.setWaitingListCode(waitingListCode);
		    appWaitingList.setWaitingListNo(waitingListNo);
		    appWaitingListRepo.save(appWaitingList);
		    reOrganiseWaitingList(oldWLCode, oldWlSlNo);
		    
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Map<String, Object> getPreviousWaitingListData(String applicationNo){
		Map<String, Object> responseMap = new HashMap<>();
		try {
			Applications app = appRepo.findByAppNo(applicationNo).orElseThrow();
			Optional<Allotments> allotment=allotmentsRepo.findByIdAndPrevWLNotNullAndPrevWLnoNotNullOrderByEntrydate(app.getAllotmentId());
			
			if(allotment.isEmpty()) {
				Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
				if(application.getAppStatus()==11) {
					ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(applicationNo).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
					responseMap.put("prevWl", appWaitingList.getWaitingListCode());
					responseMap.put("prevWlNo", appWaitingList.getWaitingListNo());
					responseMap.put("prevWlDate", appWaitingList.getEntrydate());
				}
				else
				return null;
			}
			else {
				responseMap.put("prevWl", allotment.get().getPrevWL());
				responseMap.put("prevWlNo", allotment.get().getPrevWLno());
				responseMap.put("prevWlDate", allotment.get().getPrevWLTimestamp());
			}
						
		}catch(Exception ex) {
			throw ex;
		}
		return responseMap;
	}
    
    private WaitingListEntryDTO toWaitingListEntryDTO(PublishedWaitingListEntry entry) {
    	try {
	        Applications app = appRepo.findByAppNo(entry.getAppNo()).orElse(null);
	        if (app == null) return null;
	
	        WaitingListEntryDTO dto = new WaitingListEntryDTO();
	        dto.setAppNo(entry.getAppNo());
	        dto.setSlNo(entry.getSlNo());
	        dto.setFirstName(app.getName());
	        dto.setDesignation(app.getDesignation());
	        dto.setDepartment(app.getDepartmentOrDirectorate());
	        dto.setOfficeAddress(app.getOfficeAddress());
	        dto.setScaleOfPay(app.getScaleOfPay());
	        dto.setDateOfRetirement(app.getDateOfRetirement());
	        dto.setEntrydate(app.getEntrydate() != null ? app.getEntrydate().toLocalDate() : null);
	        return dto;
    	}
    	catch(Exception ex) {
    		throw ex;
    	}
    }
    
    public String getWaitingListName(Integer wlCode) {
    	try {
    		return waitingListRepo.findByCode(wlCode).get().getList();
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    @Transactional
	public Map<String, String> moveToApprovedWLEP(String appNo, Integer actionCode, User user, String letterNo, MultipartFile file, String remarks) throws Exception {
		Map<String, String> responseMap = new HashMap<>();
		try {
			Applications application = appRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			if (application.getAppStatus() != 21 && 
				    application.getAppStatus() != 22 && 
				    application.getAppStatus() != 23) {
				throw new IllegalStateException("Application is already processed.");
			}
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(application.getAppStatus(), actionCode);			
			ApplicationsHistory move = ApplicationsHistory.builder().appNo(appNo).remarks(remarks).entrydate(new Date()).userCode(null)
					.flowCode(appFlow.getCode()).build();
			appHistoryRepo.save(move);
			UUID docCode = formService.uploadWLAREP(file, appNo);
			
			if(application.getAppStatus()==22) {
				ApplicationsWaitingList appWaitingList = appWaitingListRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Draft Waiting List entry not found"));
				if(actionCode==3) {
					appWaitingList.setIsApproved(1);
					appWaitingList.setLetterNo(letterNo);
					appWaitingList.setDocCode(docCode);
					appWaitingList.setApprovedTimestamp(new Date());;
					appWaitingListRepo.save(appWaitingList);
					reassignApprovedWaitingListNumbers(appWaitingList.getWaitingListCode());
					
					application.setWlCode(appWaitingList.getWaitingListCode());
					application.setWlSlNo(appWaitingList.getWaitingListNo());
					application.setWaitingListName(getWaitingListName(appWaitingList.getWaitingListCode()));
				}
				else {
					application.setWlCode(appWaitingList.getWaitingListCode());
					application.setWlSlNo(appWaitingList.getWaitingListNo());
					application.setWaitingListName(getWaitingListName(appWaitingList.getWaitingListCode()));
					appRepo.save(application);
					appWaitingListRepo.delete(appWaitingList);
					//delete appWaitingList---appWaitingList.setIsApproved(0);
					
				}
			}
			
			application.setLevel(appFlow.getNextLevel());
			application.setAppStatus(actionCode);
			
			appRepo.save(application);
			
			responseMap.put("formCode", docCode.toString());
			responseMap.put("detail", "Application Moved Successfully");
			
			return responseMap;
		}
		catch(Exception ex) {
			throw ex;
		}
	}

}
