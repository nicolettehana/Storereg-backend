package sad.storereg.services.appdata;

import static sad.storereg.models.auth.Role.CH;
import static sad.storereg.models.auth.Role.EST;
import static sad.storereg.models.auth.Role.USER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.dto.appdata.AllotmentOrderDownloadDTO;
import sad.storereg.dto.appdata.ApplicantDecisionDTO;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.dto.appdata.PendingAllotmentsDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsWaitingList;
import sad.storereg.models.appdata.Vacated;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.AllotmentsRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.QuartersRepository;

@Service
@RequiredArgsConstructor
public class AllotmentServices {
	
	@Value("${orders.dir}")
    private String ordersDir;
	
	private final AllotmentsRepository allotmentsRepo;
	private final QuartersRepository quartersRepo;
	private final WaitingListServices waitingListService;
	private final ApplicationHistoryServices appHistoryService;
	private final ApplicationsRepository appRepo;
	private final ReportsService reportsService;
	private final CoreServices coreService;
	private final FormServices formService;
	private final AllotmentOrderServices allotmentOrderService;
	
	@Transactional
	public Allotments allotmentRequest(Applications app, ActionRequest request, User user) {
		try {
			//Check if quarter is vacant
			Quarters quarter = quartersRepo.findByQuarterNo(request.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			if(quarter.getQuarterStatus()==1)
				throw new UnauthorizedException("Quarter not vacant");
			
			if(checkAllotmentPending(app).isPresent())
				return checkAllotmentPending(app).get();
			//if(request.getAllotmentDate()==null)
			//	throw new InternalServerError("Allotment date is required");
			String filename ="";
			if(request.getIsEproposal()!=null &&  request.getIsEproposal()==1)
				filename="";
			else 
				filename = ordersDir + "/"+app.getAppNo()+"_"+UUID.randomUUID()+".pdf";
			Allotments allotment = Allotments.builder().quarterNo(request.getQuarterNo()).appNo(app.getAppNo()).entrydate(new Date()).letterNo(request.getLetterNo()).userCode(user.getId())
					.vacateDate(app.getDateOfRetirement()).memoNo(request.getMemoNo()).filename(filename).build();
			return allotmentsRepo.save(allotment);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> checkAllotmentProcessed(Applications app) {
		try {
			return allotmentsRepo.findByIdAndIsApprovedEquals(app.getAllotmentId(),1);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> checkAllotmentPending(Applications app) {
		try {
			return allotmentsRepo.findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(app.getAllotmentId(),null);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> getAllotment(String quarterNo) {
		try {
			return allotmentsRepo.findTopByQuarterNoAndApplicantAcceptEqualsOrderByApplicantAcceptTimestamp(quarterNo, 1);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> getLatestAllotment(String appNo) {
		try {
			Applications app = appRepo.findByAppNo(appNo).orElseThrow();
			return allotmentsRepo.findById(app.getAllotmentId());
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> getApprovedAllotment(Long allotmentId) {
		try {
			Optional<Allotments> allotment = allotmentsRepo.findById(allotmentId);
			if(allotment.isPresent()) {
				if(!(allotment.get().getIsApproved()==1 && allotment.get().getCsDecisionTimestamp()!=null)) {
					throw new UnauthorizedException("No approved allotment found");
				}
			}
			return allotment;
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> getFinalApprovedAllotment(Long allotmentId) {
		try {
			Optional<Allotments> allotment = allotmentsRepo.findById(allotmentId);
			if(allotment.isPresent()) {
				if(!(allotment.get().getEsignUnderSecy()==1 && allotment.get().getUnderSecyTimestamp()!=null)) {
					throw new UnauthorizedException("No approved allotment found");
				}
			}
			return allotment;
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void allot(Applications app, User user, LocalDateTime timestamp, String letterNo) {
		try {			
			Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Allotment not found"));
			allotment.setIsApproved(1);
			allotment.setLetterNo(letterNo);
				
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			if(quarter.getQuarterStatus()==1)
				throw new UnauthorizedException("Quarter not vacant");
			quarter.setQuarterStatus(1);
			quartersRepo.save(quarter);
			Date timestamp2 = Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant());
			
			allotment.setUnderSecyTimestamp(timestamp2);
			allotment.setEsignUnderSecy(1);
			allotment.setCsDecisionTimestamp(timestamp2);
			
			
			ApplicationsWaitingList appWaitingList = waitingListService.getWaitingList(allotment.getAppNo());
			allotment.setPrevWL(appWaitingList.getWaitingListCode());
			allotment.setPrevWLno(appWaitingList.getWaitingListNo());
			allotment.setPrevWLTimestamp(appWaitingList.getEntrydate());
			allotmentsRepo.save(allotment);
			
			waitingListService.deleteWaitingList(appWaitingList);
			
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void setOrderName(Applications app, String filename) {
		try {			
			Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Allotment not found"));
			allotment.setFilename(filename);
			allotmentsRepo.save(allotment);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void csApprove(ActionRequest request, User user) {
		try {			
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow();
			Allotments allotment = allotmentsRepo.findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(app.getAllotmentId(), null).orElseThrow(()-> new ObjectNotFoundException("Initiation of allotment by CH not found"));;
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			if(quarter.getQuarterStatus()==1)
				throw new UnauthorizedException("Quarter not vacant");
			
			allotment.setIsApproved(1);
			allotment.setCsDecisionTimestamp(new Date());
			allotment.setCsRemarks(request.getRemarks());
			
			allotmentsRepo.save(allotment);
			
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void csSendBack(ActionRequest request, User user) {
		try {
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow();
			Allotments allotment = allotmentsRepo.findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(app.getAllotmentId(), null).orElseThrow(()-> new ObjectNotFoundException("Initiation of allotment by CH not found"));;
			
			allotment.setIsApproved(0);
			allotment.setCsDecisionTimestamp(new Date());
			allotment.setCsRemarks(request.getRemarks());
			
			allotmentsRepo.save(allotment);
			
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public Map<String,Object> getAllotmentDetails(String applicationNo){
		try{
			Map<String, Object> responseMap = new HashMap<>();
		
			Applications app = appRepo.findByAppNo(applicationNo).orElse(null);
			if(app==null)
				responseMap.put("detail", "Invalid application No.");
			
			Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElse(null);
			if(allotment==null) {
				responseMap.put("detail", "Invalid data.");
			}
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			responseMap.put("quarterType", quarter.getQuarterTypeCode());
			responseMap.put("quarterNo", allotment.getQuarterNo());
			
			String remark="";
			if(app.getAppStatus()==4)
				remark = "Chairman: "+appHistoryService.getRemarksByCH(applicationNo);
			if(app.getAppStatus()==10)
				remark = "Applicant: "+appHistoryService.getRemarksByUser(applicationNo);
			if(app.getAppStatus()==11)
				remark = "Chief Secretary: "+appHistoryService.getRemarksByCS(applicationNo);
			if(app.getAppStatus()==5)
				remark = "Chief Secretary: "+appHistoryService.getApprovalRemarksByCS(applicationNo);
			
			responseMap.put("remarks", remark);
			
			return responseMap;
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Map<String,Object> getAllotmentRequestDetails(String applicationNo){
		try{
			Map<String, Object> responseMap = new HashMap<>();
			Applications app = appRepo.findByAppNo(applicationNo).orElseThrow();
			Optional<Allotments> allotment = allotmentsRepo.findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(app.getAllotmentId(), null);

			if(allotment.isEmpty()) {
				responseMap.put("quarterType", null);
				responseMap.put("quarterNo", null);
				responseMap.put("letterNo", null);
				responseMap.put("memoNo", null);
				return responseMap;
			}
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.get().getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			responseMap.put("quarterType", quarter.getQuarterTypeCode());
			responseMap.put("quarterNo", allotment.get().getQuarterNo());
			
			responseMap.put("letterNo", allotment.get().getLetterNo());
			responseMap.put("memoNo", allotment.get().getMemoNo());
			
			return responseMap;
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void getAllotmentOrder(HttpServletResponse response, ActionRequest request, User user, Integer isGenerate) {
    	try {
    		Applications application = appRepo.findByAppNo(request.getAppNo()).orElseThrow(() -> new ObjectNotFoundException("Invalid application no."));
    		
    		if(!(application.getAppStatus()==14 || application.getAppStatus()==15 || application.getAppStatus()==3 || application.getAppStatus()==11 || application.getAppStatus()==10 || application.getAppStatus()==4 || application.getAppStatus()==5)){
    			throw new UnauthorizedException("Action not permitted");
    		}
    		
    		if(application.getAppStatus()==5)
    			isGenerate=0;
    		
    		if(isGenerate==1 && checkAllotmentPending(application).isPresent() )
				isGenerate=0; 
    		if(isGenerate==1 && (request.getLetterNo()==null || request.getMemoNo()==null)) {
    			throw new UnauthorizedException("Letter no. and Memo no. is required");
    		}
    		
    		Allotments allotment=null;
    		
    		if(isGenerate==1) {
    			allotment = allotmentRequest(application, request, user);
    			application.setAllotmentId(allotment.getId());
    		}
    		else {
    			if(application.getAppStatus()==5)
    				allotment = getApprovedAllotment(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("No approved allotment found."));
    			else if(application.getAppStatus()==14)
    				allotment = getFinalApprovedAllotment(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("No approved allotment found."));
    			else
    				allotment = getLatestAllotment(request.getAppNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid application no."));
    		}
    		Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid allotment quarter no."));
    		
    		if(allotment.getIsApproved()==null && user.getRole().equals(CH)) {
    			if(isGenerate==1) {
    				//Here get the uploaded form
    				reportsService.generateAllotmentOrder(response, application, allotment, quarter, request.getLetterNo(), request.getMemoNo());
    				quarter.setInAllotmentList(1);
    				quarter.setAllotmentId(allotment.getId().intValue());    				
    				quartersRepo.save(quarter);
    			}
    			else
    			{
    				reportsService.downloadAllotmentOrder(response, application, allotment);
    			}
    		}
    		else if(isGenerate==0 && ((allotment.getIsApproved()!=null && allotment.getIsApproved()==1 && user.getRole().equals(CH))  || user.getRole().equals(USER) || user.getRole().equals(EST)))
    			reportsService.downloadAllotmentOrder(response, application, allotment);
    		else
    			throw new UnauthorizedException("Error");
    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		System.out.println(ex.getLocalizedMessage());
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to generate/download Allotment Order", e);
    	}
    }
	
	@Transactional
	public void rejectApplication(String applicationNo, String remarks) {
		try {
			Applications app = appRepo.findByAppNo(applicationNo).orElseThrow();
			if(app.getAppStatus()==10) {
				Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()->new ObjectNotFoundException("Allotment not found"));
				Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
				quarter.setInAllotmentList(0);
				quarter.setAllotmentId(null);
				quartersRepo.save(quarter);
				
				return;
			}
			ApplicationsWaitingList appWaitingList = waitingListService.getWaitingList(app.getAppNo());
			
			if(app.getAppStatus()>3) {
				Allotments allotment = allotmentsRepo.findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(app.getAllotmentId(), null).orElseThrow(()->new ObjectNotFoundException("Allotment not found"));
				allotment.setPrevWL(appWaitingList.getWaitingListCode());
				allotment.setPrevWLno(appWaitingList.getWaitingListNo());
				allotment.setPrevWLTimestamp(appWaitingList.getEntrydate());
				
				allotment.setCsDecisionTimestamp(new Date());
				allotment.setCsRemarks(remarks);
				allotmentsRepo.save(allotment);
				
				Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
				quarter.setInAllotmentList(0);
				quarter.setAllotmentId(null);
				quartersRepo.save(quarter);
			}
			
			waitingListService.deleteWaitingList(appWaitingList);

		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void acceptAllotment(LocalDate occupationDate, Applications app) {
		try {
			Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()->new ObjectNotFoundException("Allotment not found"));
			if(!(allotment.getEsignUnderSecy()==1 && allotment.getUnderSecyTimestamp()!=null))
					throw new UnauthorizedException("Approved allotment not found");
			
			//allotment.setOccupationDate(occupationDate);
			allotment.setApplicantAccept(1);
			allotment.setApplicantAcceptTimestamp(new Date());
			allotmentsRepo.save(allotment);
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			quarter.setQuarterStatus(2);
			quartersRepo.save(quarter);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<Allotments> getAllotmentById(Long allotmentId) {
		try {
			return allotmentsRepo.findById(allotmentId);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void denyAllotment(String applicationNo, Long allotmentId) {
		try {
			Allotments allotment = allotmentsRepo.findById(allotmentId).orElseThrow(()->new ObjectNotFoundException("Allotment not found"));
			
			allotment.setApplicantAccept(0);
			allotment.setApplicantAcceptTimestamp(new Date());
			allotmentsRepo.save(allotment);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Page<PendingAllotmentsDTO> getPendingAllotments(Integer page, Integer size) {
        try {
        	//PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");
        	
        	PageRequest pageable = PageRequest.of(page, size);

        	List<Integer> statuses = Arrays.asList(4, 5, 10, 11, 13, 14);
            return allotmentsRepo.findPendingAllotments(statuses, pageable);

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
	
	public Page<PendingAllotmentsDTO> getCompletedAllotments(Integer page, Integer size, MyFilter filter) {
        try {
        	
        	PageRequest pageable = PageRequest.of(page, size);
        	
        	if (filter==null) {
        		List<Integer> completedStatuses = Arrays.asList(6, 7, 15);
        		String excludedPrefix = "Old%";
        		return allotmentsRepo.findCompletedAllotments(completedStatuses, excludedPrefix, pageable);
        	}
        	else {
        		List<Integer> statuses = Arrays.asList(6, 7, 15);
        		String excludedPrefix = "Old%";
        		return allotmentsRepo.findCompletedAllotmentsWithDateRange(filter.getFromDate().atStartOfDay(), filter.getToDate().atTime(LocalTime.MAX), statuses, excludedPrefix, pageable);
        	}

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch applications", e);
        }
    }
	
	public void getFileAllotmentOrder(HttpServletResponse response, AllotmentOrderDownloadDTO request, User user, Integer isGenerate) {
    	try {
    		Allotments allotment = null;
    		
    		if(request.getLetterNo()!=null)
    			allotment = allotmentsRepo.findByLetterNo(request.getLetterNo()).orElseThrow(()-> new ObjectNotFoundException("No order found with supplied letter no."));
    		
    		else if(request.getAppNo()!=null) {
    			Applications application = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid application no."));
    			allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Allotment not found"));
    		}
    		//Quarters quarter = quarterRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid allotment quarter no."));
    		
    		Applications application = appRepo.findByAppNo(allotment.getAppNo()).orElseThrow();
    		
			reportsService.downloadAllotmentOrder(response, application, allotment);

    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		System.out.println(ex.getLocalizedMessage());
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to get Allotment Order", e);
    	}
    }
	
	@Transactional
    public void uploadOrder(HttpServletResponse response, String applicationNo, String letterNo, String memoNo, String quarterNo, MultipartFile file, User user) throws Exception {
    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		throw new UnauthorizedException("Invalid Filename/Length of Filename");
	    	}
	    	
	    	//Here upload the file in the FormPath
	    	String filename = formService.uploadOrder(file, application.getAppNo());	
	    	System.out.println("one: filename: "+filename);
	    	//throw new ObjectNotFoundException("ex");
	    	
	    	Quarters quarter = quartersRepo.findByQuarterNo(quarterNo).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter no."));
			
			if(quarter.getQuarterStatus()==1)
				throw new UnauthorizedException("Quarter not vacant");
			
			if(application.getLevel()==4 || (application.getLevel()==3 && checkAllotmentProcessed(application).isPresent()))
				throw new InternalServerError("Order already generated");
			//if(request.getAllotmentDate()==null)
			//	throw new InternalServerError("Allotment date is required");
			
			Allotments allotment = null;
			ActionRequest request = new ActionRequest();
			request.setLetterNo(letterNo);
			request.setMemoNo(memoNo);
			request.setQuarterNo(quarterNo);
			if(application.getAllotmentId()==null) {
				allotment = allotmentRequest(application, request, user);
    			application.setAllotmentId(allotment.getId());
			}
			
			Optional<Allotments> optAllotment = allotmentsRepo.findById(application.getAllotmentId());
			
			if(optAllotment.isEmpty()) {
				allotment = Allotments.builder().quarterNo(quarterNo).appNo(application.getAppNo()).entrydate(new Date()).letterNo(letterNo).userCode(user.getId())
					.vacateDate(application.getDateOfRetirement()).memoNo(memoNo).filename(filename).build();
				allotmentsRepo.save(allotment);
			}else {
				allotment = optAllotment.get();
				allotment.setLetterNo(letterNo);
				allotment.setUserCode(user.getId());
				allotment.setMemoNo(memoNo);
				allotment.setFilename(filename);
			}

			application.setAllotmentId(allotment.getId());
			appRepo.save(application);
			
			allotmentOrderService.saveAllotmentOrder(allotment.getId(), null, filename, "pdf", 1, user.getUsername(), new Date());
	    	
			reportsService.downloadAllotmentOrder(response, application, allotment);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
	
	@Transactional
    public void uploadFinalOrder(HttpServletResponse response, String applicationNo, String letterNo, String memoNo, MultipartFile file, User user) throws Exception {
    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		throw new UnauthorizedException("Invalid Filename/Length of Filename");
	    	}
	    	
	    	//Here upload the file in the FormPath
	    	String filename = formService.uploadOrder(file, application.getAppNo());	
	    	System.out.println("two: filename: "+filename);
	    	//throw new ObjectNotFoundException("ex");
			
			if(application.getLevel()!=3)
				throw new InternalServerError("Action not allowed");
			
			Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(() -> new ObjectNotFoundException("Allotment not found")); 
			allotment.setLetterNo(letterNo);
			allotment.setMemoNo(memoNo);
			allotment.setFilename(filename);
			allotmentsRepo.save(allotment);
			
			allotmentOrderService.saveAllotmentOrder(allotment.getId(), null, filename, "pdf", 2, user.getUsername(), new Date());
	    	
			reportsService.downloadAllotmentOrder(response, application, allotment);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
	
	@Transactional
    public void uploadCSOrder(HttpServletResponse response, String applicationNo, String letterNo, String memoNo, MultipartFile file, User user) throws Exception {
    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		throw new UnauthorizedException("Invalid Filename/Length of Filename");
	    	}
	    	
	    	//Here upload the file in the FormPath
	    	String filename = formService.uploadOrder(file, application.getAppNo());	
	    	System.out.println("three: filename: "+filename);
	    	//throw new ObjectNotFoundException("ex");
			
			if(application.getLevel()!=4)
				throw new InternalServerError("Action not allowed");
			
			Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(() -> new ObjectNotFoundException("Allotment not found")); 
			allotment.setLetterNo(letterNo);
			allotment.setMemoNo(memoNo);
			allotment.setFilename(filename);
			allotmentsRepo.save(allotment);
			
			allotmentOrderService.saveAllotmentOrder(allotment.getId(), null, filename, "pdf", 3, user.getUsername(), new Date());
	    	
			reportsService.downloadAllotmentOrder(response, application, allotment);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
	
	@Transactional
	public Allotments vacateAllotment(Vacated request) {
		try {
			
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
			Allotments allotment=allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()->new ObjectNotFoundException("Application allotment ID not found"));
			
			if(allotment.getOccupationDate()!=null && allotment.getOccupationDate().isAfter(request.getVacateDate()))
				throw new UnauthorizedException("Vacate date cannot be before occupation date");
			
			
			if(!(allotment.getQuarterNo().equalsIgnoreCase(request.getQuarterNo())))
				throw new UnauthorizedException("Quarter No. does not belong to application allotment ID");
			
			Quarters quarter = quartersRepo.findByQuarterNo(request.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Quarter no."));
			quarter.setQuarterStatus(0);
			quarter.setInAllotmentList(0);
			quartersRepo.save(quarter);
			
			allotment.setVacateDate(request.getVacateDate());
			
			return allotment;
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public Allotments vacateAllotment2(Long allotmentId, LocalDate vacateDate) {
		try {
			
			Allotments allotment=allotmentsRepo.findById(allotmentId).orElseThrow(()->new ObjectNotFoundException("Application allotment ID not found"));
			
			if(allotment.getOccupationDate()!=null && allotment.getOccupationDate().isAfter(vacateDate))
				throw new UnauthorizedException("Vacate date cannot be before occupation date");
			
			allotment.setVacateDate(vacateDate);			
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Quarter no."));
			quarter.setQuarterStatus(0);
			quarter.setInAllotmentList(0);
			quartersRepo.save(quarter);
			
			return allotmentsRepo.save(allotment);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
    public void uploadAcceptanceLetter(HttpServletResponse response, String applicationNo, char code, MultipartFile file, User user) throws Exception {
    	
    	try {
	    	Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
	    	
	    	if(!(user.getRole().name().equals("EST") || application.getUserCode().equals(user.getId())))
	    		throw new UnauthorizedException("Action not allowed");
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		throw new UnauthorizedException("Invalid Filename/Length of Filename");
	    	}
	    	
	    	//Here upload the file in the FormPath
	    	String filename = formService.uploadApplicantDecision(file, application.getAppNo()).getFormCode().toString();	
			
			Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(() -> new ObjectNotFoundException("Allotment not found")); 
			allotment.setApplicantLetter(filename);
			allotmentsRepo.save(allotment);
			
			allotmentOrderService.saveApplicantLetter(allotment.getId(), code, filename, "pdf", user.getUsername(), new Date());
	    	
			reportsService.downloadAllotmentOrder(response, application, allotment);
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
	
	@Transactional
	public void markAsOccupied(LocalDate occupationDate, String applicationNo) {
		try {
			Applications application = appRepo.findByAppNo(applicationNo).orElseThrow(() -> new ObjectNotFoundException("Invalid Application no."));
			Allotments allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(() -> new ObjectNotFoundException("Allotment not found"));
			
			if(allotment.getApplicantLetter()==null)
				throw new UnauthorizedException("Acceptance Letter not uploaded");
			
			allotment.setOccupationDate(occupationDate);
			allotmentsRepo.save(allotment);
			
			Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow();
			quarter.setQuarterStatus(1);
			quartersRepo.save(quarter);
			
		}catch(Exception ex) {
			throw ex;
		}
	}

	public void getApplicantLetter(HttpServletResponse response, AllotmentOrderDownloadDTO request, User user) {
    	try {
    		Allotments allotment = null;
    		Applications application = null;
    		
    		//if(request.getLetterNo()!=null)
    		//	allotment = allotmentsRepo.findByApplicantLetter(request.getLetterNo()).orElseThrow(()-> new ObjectNotFoundException("No letter was uploaded."));
    		
    		//else 
    		if(request.getAppNo()!=null) {
    			 application = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()-> new ObjectNotFoundException("Invalid application no."));
    			allotment = allotmentsRepo.findById(application.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Allotment not found"));
    		}
    		else {
    			throw new UnauthorizedException("appNo is required");
    		}
    		//Quarters quarter = quarterRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid allotment quarter no."));
    		
    		//Applications application = appRepo.findByAppNo(allotment.getAppNo()).orElseThrow();
    		
			reportsService.downloadApplicantLetter(response, application, allotment);
			
    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		System.out.println(ex.getLocalizedMessage());
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to get Download Applicant Letter", e);
    	}
    }
	
	public Integer getNoOfPendingAllotments() {
		try {
			return quartersRepo.countByQuarterStatus(2);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void rejectAllotEP(Applications app, LocalDateTime timestamp) {
		try {			
			Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(()-> new ObjectNotFoundException("Allotment not found"));
			allotment.setIsApproved(0);
			Date timestamp2 = Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant());
			allotment.setUnderSecyTimestamp(timestamp2);
			allotment.setEsignUnderSecy(0);
			allotment.setCsDecisionTimestamp(timestamp2);
			
			ApplicationsWaitingList appWaitingList = waitingListService.getWaitingList(allotment.getAppNo());
			allotment.setPrevWL(appWaitingList.getWaitingListCode());
			allotment.setPrevWLno(appWaitingList.getWaitingListNo());
			allotment.setPrevWLTimestamp(appWaitingList.getEntrydate());
			allotmentsRepo.save(allotment);
			
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void createAllotment(String appNo, User user, Integer isGenerate, String quarterNo, String letterNo) {
    	try {
    		Applications application = appRepo.findByAppNo(appNo).orElseThrow(() -> new ObjectNotFoundException("Invalid application no."));
    		
    		if(!(application.getAppStatus()==3)){
    			throw new UnauthorizedException("Action not permitted");
    		}

    		if((isGenerate==1 || isGenerate==2) && checkAllotmentPending(application).isPresent() )
				isGenerate=0; 
    		
    		Allotments allotment=null;
    		
    		System.out.println("isGenerate: "+isGenerate);
    		
    		if(isGenerate==1 || isGenerate ==2) {
    			ActionRequest actionRequest = new ActionRequest();
    			actionRequest.setQuarterNo(quarterNo);
    			actionRequest.setLetterNo(letterNo);
    			if(isGenerate==2)
    				actionRequest.setIsEproposal(1);
    			allotment = allotmentRequest(application, actionRequest, user);
    			application.setAllotmentId(allotment.getId());
    		}
    		
    		Quarters quarter = quartersRepo.findByQuarterNo(allotment.getQuarterNo()).orElseThrow(()->new ObjectNotFoundException("Invalid allotment quarter no."));
    		quarter.setInAllotmentList(1);
    		quarter.setAllotmentId(allotment.getId().intValue());    				
			quartersRepo.save(quarter);
    		
    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		System.out.println(ex.getLocalizedMessage());
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to crate allotment", e);
    	}
    }
	
	@Transactional
	public void cancelAllotment(String appNo, Long userCode, String remarks) {
		try {
				Applications app = appRepo.findByAppNo(appNo).orElseThrow(()-> new ObjectNotFoundException("Invalid appNo"));
				Allotments allotment = allotmentsRepo.findById(app.getAllotmentId()).orElseThrow(() -> new ObjectNotFoundException("Allotment not found"));
				allotment.setIsApproved(0);
				app.setAllotmentId(null);
				
				appRepo.save(app);
				allotmentsRepo.save(allotment);
				
				//Put in application history
				
				appHistoryService.save(appNo, remarks, new Date(), userCode, 40);//40 - from 15 to 7 (Allotment Cancelled)
				
			}catch(Exception ex) {
			throw ex;
		}
	}
}
