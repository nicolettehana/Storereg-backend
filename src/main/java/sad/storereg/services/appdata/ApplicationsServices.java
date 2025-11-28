package sad.storereg.services.appdata;

import static sad.storereg.models.auth.Role.ADMIN;
import static sad.storereg.models.auth.Role.CH;
import static sad.storereg.models.auth.Role.EST;
import static sad.storereg.models.auth.Role.USER;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ActionDTO;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsHistory;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationFlow;
import sad.storereg.models.master.ApplicationStatus;
import sad.storereg.repo.appdata.ApplicationHistoryRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.ApplicationFlowRepository;
import sad.storereg.repo.master.ApplicationLevelRepository;
import sad.storereg.repo.master.ApplicationStatusRepository;

@Service
@RequiredArgsConstructor
public class ApplicationsServices {
	
	private final ApplicationsRepository appRepo;
	private final DecisionServices decisionService;	
	private final ApplicationFlowRepository appFlowRepo;	
	private final ApplicationLevelRepository appLevelRepo;	
	private final ApplicationStatusRepository appStatusRepo;	
	private final CoreServices coreService;	
	private final ReportsService reportsService;	
	private final ApplicationHistoryRepository appHistoryRepo;	
	private final FormServices formService;	
	private final WaitingListServices waitingListService;
	private final AllotmentServices allotmentService;
	private final ServiceNotification serviceNotification;

	public String generateApplicationNo() {
		
		//String count = new DecimalFormat("00000").format(appRepo.count()+1);
		//return "GADB-"+Integer.toString(Year.now().getValue() % 100)+"-"+count+"";
		
		// Fetch the last application number from the database
        Optional<String> lastAppNoOpt = appRepo.findLastApplicationNo();
        
        int nextSerialNumber = 1; // Default if no record exists

        if (lastAppNoOpt.isPresent()) {
            String lastAppNo = lastAppNoOpt.get();
            
            // Extract the last five digits (serial number)
            Pattern pattern = Pattern.compile("\\d{5}$");
            Matcher matcher = pattern.matcher(lastAppNo);

            if (matcher.find()) {
                nextSerialNumber = Integer.parseInt(matcher.group()) + 1;
            }
        }

        // Format the new serial number with leading zeros
        String nextSerial = new DecimalFormat("00000").format(nextSerialNumber);

        // Get last two digits of the current year
        String year = Integer.toString(Year.now().getValue() % 100);

        // Construct the new application number
        return "GADB-" + year + "-" + nextSerial;
	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> createUpdateApplicationDraft(Applications application, Long userCode, Integer appStatus) {
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			//Create New Application/Draft
			if(application.getAppNo()==null) {
				
				application.setAppNo(generateApplicationNo());
				application.setUserCode(userCode);
				application.setLevel(appStatus==0?0:-1);
				application.setAppStatus(appStatus);
				application.setEntrydate(LocalDateTime.now());
				
				appRepo.save(application);
				
				responseMap.put("detail", application.getAppStatus()!=null && application.getAppStatus() == 0 ?"Draft Saved":"Application Submitted");
				responseMap.put("applicationNo", application.getAppNo());
				
				return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
			}
			else {
				responseMap.put("detail", "Submission failed");
				
				return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Unable to submit/edit application", e);
		}
	}
	
	@Transactional
	public ApplicationsHistory cancelApplication(ActionRequest request, User user) {
		
		return(decisionService.cancel(request.getAppNo(), request.getRemarks(), user.getId()));
	}
	
	public Page<Applications> getApplications(User user, Integer page, Integer size) {
        try {
        	
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

            Page<Applications> pagedApplications;

            if (user.getRole().equals(USER)) {
                pagedApplications = appRepo.findAllByUserCodeAndAppStatusNot(user.getId(), 8, pageable);
            }
            else {
                int level = -1;
                if (user.getRole().equals(CH)) level = 1;
                else if (user.getRole().equals(EST)) level = 5;
                
                if(level == 1)
                	pagedApplications = appRepo.findAllByLevelAndAppStatus(level, 1, pageable);
                else
                	pagedApplications = appRepo.findAllByLevel(level, pageable);
            }
            
            // Map each application to include actions
            return pagedApplications.map(application -> {

            	if(application.getAppStatus()==3 && application.getWlSlNo()==null) {
            		application.setAppStatus(2);
            	}
            	
            	if(application.getAppStatus() != 2 && application.getAppStatus() != 3 && application.getAppStatus() !=5 && application.getAppStatus() != 12 && application.getAppStatus() != 13)
            	{
            		application.setWlSlNo(null);
            		application.setWaitingListName(null);
            		application.setWlCode(null);
            		application.setWlVersion(null);
            	}
            	
                List<ActionDTO> actions = getAvailableActions(application, user.getRole().name());

                //if(application.getAppStatus()!=4)
                	application.setActions(actions);

                return application;
            });

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
	
	public Page<Applications> getAllApplications(Integer page, Integer size, MyFilter filter) {
        try {
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

            Page<Applications> pagedApplications=null;
            if(filter.getFromDate()==null && filter.getToDate()==null)
            	pagedApplications = appRepo.findAllByAppStatusGreaterThan(0, pageable);
            else if(filter.getFromDate()!=null && filter.getToDate()!=null)
            	pagedApplications = appRepo.findAllByAppStatusGreaterThanAndUploadTimestampGreaterThanEqualAndUploadTimestampLessThanEqual(0, filter.getFromDate().atStartOfDay(), filter.getToDate().atTime(LocalTime.MAX), pageable);
            
            return pagedApplications;

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
	
	public Page<Applications> getQCApplications(User user, Integer page, Integer size) {
        try {
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

            Page<Applications> pagedApplications;

            int level = 1;

            pagedApplications = appRepo.findAllByLevelAndAppStatus(level, 10, pageable);
            
            // Map each application to include actions
            return pagedApplications.map(application -> {

                List<ActionDTO> actions = getAvailableActions(application, user.getRole().name());

                application.setActions(actions);

                return application;
            });

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
	
//	public Page<Applications> getReturnedApplications(User user, Integer page, Integer size) {
//        try {
//        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");
//
//            Page<Applications> pagedApplications;
//
//            int level = 2;
//
//            pagedApplications = appRepo.findAllByLevelAndAppStatus(level, 11, pageable);
//            
//            // Map each application to include actions
//            return pagedApplications.map(application -> {
//
//                List<ActionDTO> actions = getAvailableActions(application, user.getRole().name());
//
//                application.setActions(actions);
//
//                return application;
//            });
//
//        } catch (Exception e) {
//            throw new InternalServerError("Unable to fetch applications", e);
//        }
//    }
	
//	public Page<Applications> getApplicationsForFinalApproval(User user, Integer page, Integer size) {
//        try {
//        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");
//
//            Page<Applications> pagedApplications;
//
//            int level = 3;
//
//            pagedApplications = appRepo.findAllByLevelAndAppStatus(level, 5, pageable);
//            
//            // Map each application to include actions
//            return pagedApplications.map(application -> {
//
//                List<ActionDTO> actions = getAvailableActions(application, user.getRole().name());
//
//                application.setActions(actions);
//
//                return application;
//            });
//
//        } catch (Exception e) {
//            throw new InternalServerError("Unable to fetch applications", e);
//        }
//    }
	
	// Helper method to fetch actions for an application (same as provided earlier)
    public List<ActionDTO> getAvailableActions(Applications application, String userRole) {
    	
        List<ApplicationFlow> allowedFlows = appFlowRepo.findAllByFromStatus(application.getAppStatus());
        List<ActionDTO> actions = new ArrayList<>();
        
        for (ApplicationFlow flow : allowedFlows) {
            //ApplicationLevels level2 = appLevelRepo.findByRole(userRole);
            int level = -1;
            if(userRole.equals(CH.name()) && application.getAppStatus()!=5)
            	level = 1;
            else if(userRole.equals(CH.name()) && application.getAppStatus()==5)
            	level=2;
            else
            	level = appLevelRepo.findByRole(userRole).getLevel();
            if(level==application.getLevel() && flow.getToStatus()!=7) {
            	if(userRole.equals("USER") && flow.getToStatus()==21){
            		
            	}
            	else {
            		Integer isEnabled = 1;
            		ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());
            		actions.add(new ActionDTO(status.getAction(), isEnabled, status.getStatusCode()));
            	}
            }
            else {
            	if(userRole.equals(USER.name()) && flow.getToStatus()==7){ //If action is Cancel
            		ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());
            		
            		//Allowed to Cancel, enabled Cancel Button
            		if(application.getAppStatus()>=1 && application.getAppStatus()<=4)
            			actions.add(new ActionDTO(status.getAction(), 1, status.getStatusCode()));
            		else //Not allowed to Cancel, disable Cancel Button
            			actions.add(new ActionDTO(status.getAction(), 0, status.getStatusCode()));
            	}
            	if(userRole.equals(CH.name()) && flow.getToStatus()==7){ 
            		Integer isEnabled = 1;
                	ApplicationStatus status = appStatusRepo.findByStatusCode(flow.getToStatus());
                	actions.add(new ActionDTO(status.getAction(), isEnabled, status.getStatusCode()));
            	}
            	
            }	
            
        }
        return actions;
    }
    
    public List<ApplicationStatus> getApplicationStatus() {
    	return appStatusRepo.findAll();
    }
    
    @Transactional
	public ApplicationsHistory discardDraft(ActionRequest request, User user) {
		
		return(decisionService.discard(request.getAppNo(), request.getRemarks(), user.getId()));
	}
    
    public Map<String, Object> getSummary(Long userCode){
    	
    	Optional<Applications> application = appRepo.findTopByUserCodeAndAppStatusGreaterThanAndAppStatusLessThanOrderByEntrydateDesc(userCode,0,7);
    	
    	Map<String, Object> responseMap = new HashMap<>();
    	
        if(application.isEmpty())
        	responseMap.put("detail", "There are no applications");
        else {
        	responseMap.put("appNo",application.get().getAppNo());
        	responseMap.put("appStatus", coreService.getStatus(application.get().getAppStatus()));
        }
        
        List<Applications> pendingForAction = appRepo.findAllByUserCodeAndLevelAndUploadTimestampNotNull(userCode, 0);
        //List<Applications> inProgress = appRepo.findAllByUsernameAndAppStatusGreaterThanAndAppStatusLessThan(username, 0, 5);
        List<Applications> completed = appRepo.findAllByUserCodeAndAppStatusEqualsOrUserCodeAndAppStatusEqualsOrUserCodeAndAppStatusEquals(userCode, 6,userCode, 7,userCode, 15);
        
        responseMap.put("pendingForAction", pendingForAction.size());
        responseMap.put("completed", completed.size());
        
        List<String> messages = new ArrayList<>();
        
        
        if(pendingForAction.size()>0) {
        	for (Applications app : pendingForAction) {
        		String message = "";
        		if(app.getAppStatus()==9) {
        			message=message+"Application no.: "+app.getAppNo()+" is pending with you for action. Please upload a clear signed and sealed copy of the form to complete the application.";
        			messages.add(message);
        		}
        		else if(app.getAppStatus()==14) {
        			message = message+"Application no.: "+app.getAppNo()+" is pending with you for action. Please accept/reject the allotment.";
        			messages.add(message);
        		}
        	}
        }
        responseMap.put("message", messages);
        
        return responseMap;
    }
    
    public void generateForm(HttpServletResponse response, String applicationNo, User user) {
    	try {
    		Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
    		
    		if(user.getRole().equals(USER) && !(user.getId().equals(application.getUserCode())))
    			throw new UnauthorizedException("Application does not belong to user");
    		
    		if(application.getEntrydate()==null)
				throw new UnauthorizedException("Error");
    		
    		if((application.getAppStatus()>=1 && application.getAppStatus()<=7) || (application.getAppStatus()>=10)) {
    			//Here get the uploaded form
    			reportsService.downloadForm(response, application);
    			//return;
    		}
    		else if(application.getAppStatus()==0 || application.getAppStatus()==8)
    			reportsService.generateForm(response, application);
    		else
    			throw new UnauthorizedException("Error");
    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to generate Application Form", e);
    	}
    }
    
    @Transactional
    public ResponseEntity<Map<String, String>> uploadForm(String applicationNo, MultipartFile file, User user) {
    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	
	    	if(!(user.getId()).equals(application.getUserCode()))
	    		throw new UnauthorizedException("Application does not belong to user");
	    	
	    	Map<String,String> responseMap = new HashMap<>();
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		responseMap.put("message", "Invalid Filname/Length of Filename");
				return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST); 
	    	}
	    	
	    	ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(application.getAppStatus(), 1);
	    	
	    	ApplicationsHistory uploadHistory = ApplicationsHistory.builder().appNo(application.getAppNo()).remarks("").entrydate(new Date()).userCode(user.getId())
					.flowCode(appFlow.getCode()).build();
	    	
	    	appHistoryRepo.save(uploadHistory);
	    	
	    	//Here upload the file in the FormPath
	    	String formCode = formService.uploadFile(file, application.getAppNo());
	    	
	    	//Update the application data also with formCode
	    	if(application.getUploadTimestamp()==null)
	    		application.setUploadTimestamp(LocalDateTime.now());
	    	
	    	application.setFormUpload(formCode);
	    	application.setLevel(appFlow.getNextLevel());
	    	application.setAppStatus(appFlow.getToStatus());
	    	
	    	appRepo.save(application);    	
	    	
	    	Map<String, String> response = new HashMap<>();
	    	response.put("detail", "Successfully uploaded");
	    	return new ResponseEntity<>(response, HttpStatus.OK);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }

    @Transactional
    public ResponseEntity<Map<String, String>> action(ActionRequest request, User user, HttpServletRequest httpRequest){
    	try {
	    	Map<String, String> responseMap = new HashMap<>();
	    	
	    	if(request.getActionCode()==2) {
	    		return waitingListService.moveToWL(request, user);
	    	}
	    	
	    	if(request.getActionCode()==3 || request.getActionCode()==12) { //Publish the application (This i can make one application at a time later)
	    		return waitingListService.moveToApprovedWL(request, user);
	    	}
	    	
	    	if(request.getActionCode()==4) {
	    		return decisionService.allot(request, user, httpRequest);
	    	}
	    	if(request.getActionCode()==14) {
	    		return decisionService.allot(request, user, httpRequest);
	    	}
	    	if(request.getActionCode()==15)
	    		return decisionService.acceptAllotment(request, user);
	    	if(request.getActionCode()==9) {
	    		return decisionService.formReUpload(request, user);
	    	}
	    	if(request.getActionCode()==10) {
	    		return decisionService.requestChangeWL(request, user);
	    	}
	    	
	    	if(request.getActionCode() == 6) {
	    		
	    		if(user.getRole().equals(USER) || user.getRole().equals(ADMIN)) {
	    			responseMap.put("detail", "Action not allowed");
	    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
	    		}
	    		if(user.getRole().equals(CH)) {
	    			allotmentService.rejectApplication(request.getAppNo(), request.getRemarks());
	    		}	    		
	    		ApplicationsHistory reject = ApplicationsHistory.builder().appNo(request.getAppNo()).remarks(request.getRemarks()).entrydate(new Date()).userCode(user.getId())
						.flowCode(request.getActionCode()).build();
				
				appHistoryRepo.save(reject);
				
				Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid application no."));
				app.setLevel(-1);
				if(app.getAppStatus()==10)
					app.setAppStatus(20);
				else
					app.setAppStatus(request.getActionCode());
				
				appRepo.save(app);
				
				serviceNotification.sendSms(request.getActionCode(), request.getAppNo(), user.getUsername(), httpRequest);
				responseMap.put("detail", "Application Rejected successfully");
				return new ResponseEntity<>(responseMap, HttpStatus.OK);
	    	}
	    	
	    	responseMap.put("detail", "Not coded yet from backend");
			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    	} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to perform action", e);
		}
    }

    public ResponseEntity<Map<String, String>> getRemarks(String applicationNo, User user) {
    	try {
	    	Map<String, String> responseMap = new HashMap<>();
	    	
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
	    	
	    	responseMap.put("remark", getRemarks(application));
	    	
	    	return new ResponseEntity<>(responseMap, HttpStatus.OK);
    	} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
    }
    
    private String getRemarks(Applications application) {
    	String remark = "";
	    	if(application.getAppStatus()==9 || application.getAppStatus()==6 || application.getAppStatus()==11) { //Re-upload Enclosure
	    		Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoOrderByEntrydateDesc(application.getAppNo());
	    		remark = appHistory.isPresent()?appHistory.get().getRemarks():"--";
	    	}
    	return remark;
    }
    
    public ResponseEntity<Map<String, String>> getRemarksDA(String applicationNo, User user) {
    	try {
	    	Map<String, String> responseMap = new HashMap<>();
	    	
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
	    	
	    	responseMap.put("remark", getRemarksDA(application));
	    	
	    	return new ResponseEntity<>(responseMap, HttpStatus.OK);
    	} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
    }
    
    private String getRemarksDA(Applications application) {
    	String remark = "";
	    Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(application.getAppNo(),2);
	    remark = appHistory.isPresent()?"Dealing Asst.: "+appHistory.get().getRemarks():"U/A";
	    	
    	return remark;
    }
    
    public Optional<Applications> getApplication(Long allotmentId) {
    	System.out.println("Allotment ID:"+allotmentId);
    	return appRepo.findByAllotmentId(allotmentId);
    }
    
    public Applications setVacateStatus(Integer status, Long allotmentId) {
    	try {
    		Optional<Applications> application = appRepo.findByAllotmentId(allotmentId);
    		if(application.isEmpty())
    			throw new ObjectNotFoundException("Application not found");
    		application.get().setVacateStatus(status);
    		return appRepo.save(application.get());
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    @Transactional
    public void updateSlNo(String appNo, Integer newListCode, Integer newVersion, Integer newSlNo) {
    	try {
    		Optional<Applications> app = appRepo.findByAppNo(appNo);
    		if(app.isEmpty())
    			throw new ObjectNotFoundException("Application not found");
    		app.get().setWlCode(newListCode);
    		app.get().setWlSlNo(newSlNo);
    		app.get().setWlVersion(newVersion);
    		app.get().setWaitingListName(waitingListService.getWaitingListName(newListCode));
    		appRepo.save(app.get());
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    @Transactional
    public ResponseEntity<Map<String, String>> approvalOrderUpload(String applicationNo, MultipartFile file, User user) {
    	
    	try {
	    	
	    	if(!(user.getRole().name().equals("CH")))
	    		throw new UnauthorizedException("User not authorized");
	    	
	    	Map<String,String> responseMap = new HashMap<>();
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		responseMap.put("message", "Invalid Filname/Length of Filename");
				return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST); 
	    	}
	    	
	    	
	    	//Here upload the file in the FormPath
	    	String formCode = formService.uploadApprovalOrder(file, applicationNo);  	
	    	
	    	Map<String, String> response = new HashMap<>();
	    	response.put("detail", "Successfully uploaded");
	    	response.put("docCode", formCode);
	    	return new ResponseEntity<>(response, HttpStatus.OK);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    @Transactional
    public Applications setLevelAndStatus(int level, int actionCode, String appNo) {
    	try {
    		Applications app = appRepo.findByAppNo(appNo).orElseThrow(()-> new ObjectNotFoundException("Invalid appNo"));
    		app.setLevel(level);
    		app.setAppStatus(actionCode);
    		app.setEntrydate(LocalDateTime.now());
    		return appRepo.save(app);    		
    		
    	}catch(Exception ex) {
    		throw ex;
    	}
    	
    }
    
    @Transactional
    public Map<String, String> uploadEProposalForm(String applicationNo, MultipartFile file, String remarks, Integer actionCode) throws Exception {    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	Map<String,String> responseMap = new HashMap<>();
	    	if (application.getAppStatus() != 21 && 
				    application.getAppStatus() != 22 && 
				    application.getAppStatus() != 23) {
				throw new IllegalStateException("Application is already processed.");
			}
	    	if(actionCode!=6) {
		    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
		    	
		    	if (!(Boolean)data.get("status")) {
		    		responseMap.put("message", "Invalid Filname/Length of Filename");
		    		throw new BadRequestException("Invalid Filename/Lemgth of Filename");
		    	}
	    	}
	    	ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(application.getAppStatus(), actionCode);
	    	ApplicationsHistory uploadHistory = ApplicationsHistory.builder().appNo(application.getAppNo()).remarks(remarks).entrydate(new Date()).userCode(null)
					.flowCode(appFlow.getCode()).build();
	    	appHistoryRepo.save(uploadHistory);
	    	//Here upload the file in the FormPath
	    	String formCode="";
	    	if(actionCode!=6)
	    		formCode = formService.uploadFile(file, application.getAppNo());
	    	if(actionCode == 1) {
	    		//Update the application data also with formCode
	    		if(application.getUploadTimestamp()==null)
	    			application.setUploadTimestamp(LocalDateTime.now());
	    		
	    		application.setFormUpload(formCode);
	    	}
	    	application.setLevel(appFlow.getNextLevel());
	    	application.setAppStatus(appFlow.getToStatus());
	    	
	    	appRepo.save(application);    	
	    	
	    	Map<String, String> response = new HashMap<>();
	    	response.put("detail", "Successfully uploaded");
	    	response.put("formCode", formCode);
	    	return response;
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    public Applications validateStatus(String appNo, String requestType) {
    	try {
    		Applications app = appRepo.findByAppNo(appNo).orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appNo"));
    		if(requestType.equals("WLA")) {
    			if(app.getAppStatus()!=2)
    				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application not in draft");
    		}else if(requestType.equals("AA")) {
    			if(app.getAppStatus()!=3)
    				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application not in approved Waiting Lists");
    		}
    		return app;
    	}catch(Exception ex) {
    		throw ex;
    	}
    	
    }
    
    public Page<Applications> getReturnedApplications(User user, Integer page, Integer size) {
        try {
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

            Page<Applications> pagedApplications;

            int level = 1;

            pagedApplications = appRepo.findAllByLevelAndAppStatus(level, 11, pageable);
            
            // Map each application to include actions
            return pagedApplications.map(application -> {

                List<ActionDTO> actions = getAvailableActions(application, user.getRole().name());

                application.setActions(actions);

                return application;
            });

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
    
    public Applications getApp(String appNo) {
    	try {
    		return appRepo.findByAppNo(appNo).orElseThrow();
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
}
