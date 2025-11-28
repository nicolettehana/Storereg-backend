package sad.storereg.services.appdata;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.Column;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import sad.storereg.logs.EProposalIncomingRequests;
import sad.storereg.logs.EProposalIncomingRequestsRepository;
import sad.storereg.models.appdata.EProposalRequest;
import sad.storereg.models.auth.User;
import sad.storereg.repo.appdata.EProposalRequestRepository;

@Service
@RequiredArgsConstructor
public class EProposalService {
	
	private final CoreServices coreService;
	private final EProposalRequestRepository eProposalRequestRepo;
	private final ApplicationsServices appService;
	private final EProposalIncomingRequestsRepository requestLogRepo;
	private final WaitingListServices waitingListService;
	private final DecisionServices decisionService;
	private final EProposalIncomingRequestsRepository epsIncomingReqRepo;
	
	public void validateStatusUpdate(String requestId, String status, String fileMovementNote, String department, String designation, String office, String userName, String movementDateTime) {
		try {
            Long.parseLong(requestId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid requestId: must be a number");
        }
		try {
            LocalDateTime.parse(movementDateTime); // assumes ISO-8601 format, e.g. "2024-06-25T14:30:00"
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timestamp: must be in ISO format (e.g., 2025-06-25T14:30:00)");
        }
		 // Validate required string fields are not blank
	    if (!StringUtils.hasText(status)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be blank");
	    }

	    if (!StringUtils.hasText(fileMovementNote)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File movement note must not be blank");
	    }

	    if (!StringUtils.hasText(department)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department must not be blank");
	    }

	    if (!StringUtils.hasText(designation)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Designation must not be blank");
	    }

	    if (!StringUtils.hasText(office)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Office must not be blank");
	    }

	    if (!StringUtils.hasText(userName)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User name must not be blank");
	    }
	}
	
	public void validateInput(String requestId, String status, String userCode, String timestamp, String remarks, MultipartFile file, String letterNo) {
    	try {
            Long.parseLong(requestId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid requestId: must be a number");
        }

        Set<String> validStatuses = Set.of("A", "R", "C");
        if (!validStatuses.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: must be A, R, or C");
        }
        if(status.equals("R") && remarks==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejected requests need remarks");
        }
        
        if(userCode!=null)
        try {
            Integer.parseInt(userCode);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userCode: must be an integer");
        }

        try {
            LocalDateTime.parse(timestamp); // assumes ISO-8601 format, e.g. "2024-06-25T14:30:00"
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timestamp: must be in ISO format (e.g., 2025-06-25T14:30:00)");
        }
        
        if(status.equals("A"))
        {
	        if (file == null || file.isEmpty() || file.getSize() <= 0) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file: file must not be empty");
	        }
	        if (letterNo==null) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "letterNo is required");
	        }
	        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Filename/Length of Filename");
	    	}
	    	if (!file.getContentType().equalsIgnoreCase("application/pdf")) {
	    	    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type: only PDF files are allowed");
	    	}
        }
    }
	
	public void validateEProposalRequest(EProposalRequest request) {
        
		if(request.getAppNo()!=null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appNo must be null");

        Set<String> validStatuses = Set.of("FOA", "WLA", "AA");
        if (!validStatuses.contains(request.getRequestType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: must be FOA, WLA, or AA");
        }

        if(request.getOutgoingUserDepartment()==null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "outgoingUserDepartment is required");
        
        if(request.getOutgoingUserDesignation()==null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "outgoingUserDesignation is required");
        
        if(request.getOutgoingUserOffice()==null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "outgoingUserOffice is required");
        
        if(request.getRequestType().equals("AA")) {
        	if(request.getQuarterNo()==null)
    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quarterNo is required");
        	if(request.getQuarterType()==null)
    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quarterType is required");	
        }
        if(request.getRequestType().equals("WLA")) {
        	if(request.getWlNo()==null)
    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wlNo is required");
        }
    }

	@Transactional
	public EProposalRequest createRequest(EProposalRequest request) {
		try {
			if(request.getRequestType()!="WLA")
				request.setWlNo(null);
			
			request.setFormCode(null);
			if(request.getRequestType()!="AA") {
				request.setQuarterType(null);
		    	request.setQuarterNo(null);
		    }
		    request.setLetterNo(null);
		    request.setMemoNo(null);
		    request.setIncomingTimestamp(null);
		    request.setIncomingUserCode(null);
		    request.setStatus(null);
		    request.setRemarks(null);
		    request.setIncomingUserName(null);
		    request.setOutgoingTimestamp(LocalDateTime.now());
		    System.out.println("Request: "+request);
			return eProposalRequestRepo.save(request);
			
		}catch(Exception ex) {
			throw ex;
		}
	}

	@Transactional
	public void processCallback(String remarks, String status,
			String requestId, String userCode, String timestamp, String letterNo, String memoNo, String userName,
    		String userDepartment, String userDesignation, MultipartFile file, User user, HttpServletRequest httpRequest) throws Exception {
		try {
			EProposalRequest request = eProposalRequestRepo.findById(Long.parseLong(requestId)).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RequestId"));
			String filePath=null;
			
			if(request.getRequestType().equals("FOA")) {
				//upload the file first
				if(status.equals("A")) {
					Map<String,String> response = appService.uploadEProposalForm(request.getAppNo(), file, userCode+", "+userName+", "+userDepartment+", "+userDesignation+" at "+timestamp+": "+remarks,1);
					filePath = response.get("formCode");
				}
				else if(status.equals("R")) {
					Map<String,String> response = appService.uploadEProposalForm(request.getAppNo(), file, remarks,6);
					filePath = response.get("formCode");
				}
			}
			else if(request.getRequestType().equals("WLA")) { //actionCode = 3
				if(status.equals("A")) {
					Map<String,String> response =  waitingListService.moveToApprovedWLEP(request.getAppNo(), 3, user, request.getLetterNo(), file, null);
					filePath = response.get("formCode");
				}
				else if(status.equals("R")) {//11 will be removed from draft list, go to inbox of dept
					Map<String,String> response =  waitingListService.moveToApprovedWLEP(request.getAppNo(), 11, user, request.getLetterNo(), file, remarks);
					filePath = response.get("formCode");
				}
			}
			else { //AA actionCode = 4
				if(status.equals("A")) {
					Map<String,String> response = decisionService.allotEP(request.getAppNo(), 4, file, LocalDateTime.now(), request.getLetterNo(), remarks, httpRequest);
					filePath = response.get("formCode");
				}
				else if(status.equals("R")) {
					Map<String,String> response = decisionService.allotEP(request.getAppNo(), 3, file,LocalDateTime.now(), request.getLetterNo(), remarks, httpRequest);
					filePath = response.get("formCode");
				}
				//upload the file first
				//do action related works
			}
			updateEProposalRequest(request, filePath, userCode, userDepartment, userDesignation, userName, letterNo, memoNo,
					remarks, status, timestamp);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	private void updateEProposalRequest(EProposalRequest request, String formCode, String userCode, String department, String designation, String userName, String letterNo, String memoNo,
			String remarks, String status, String timestamp) {
		try {
			UUID fCode=null;
			if(!formCode.equals(""))
				fCode = UUID.fromString(formCode);
			request.setFormCode(fCode);
			request.setIncomingTimestamp(LocalDateTime.now());
			request.setIncomingUserCode(userCode==null?null:Integer.parseInt(userCode));
			request.setIncomingUserDepartment(department);
			request.setIncomingUserDesignation(designation);
			request.setIncomingUserName(userName);
			request.setLetterNo(letterNo);
			request.setMemoNo(memoNo);
			request.setRemarks(remarks);
			request.setStatus(status);
			request.setStatusTimestamp(LocalDateTime.parse(timestamp));
			eProposalRequestRepo.save(request);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void logEntry(String remarks, String status,
			String requestId, String letterNo, String memoNo, String timestamp) throws Exception {
		try {
			EProposalIncomingRequests logEntry = new EProposalIncomingRequests();

	        logEntry.setRemarks(remarks);
	        logEntry.setStatus(status);
	        logEntry.setRequestId(Long.parseLong(requestId));
	        //logEntry.setUserCode(Long.parseLong(userCode));

	        logEntry.setTimestamp(LocalDateTime.parse(timestamp)); // Make sure it's in ISO-8601 format

	        logEntry.setLetterNo(letterNo);
	        logEntry.setMemoNo(memoNo);
	        //logEntry.setUserName(userName);
	        //logEntry.setUserDepartment(userDepartment);
	        //logEntry.setUserDesignation(userDesignation);
	        logEntry.setEntrydate(LocalDateTime.now());
	        logEntry.setType("Final Status");
	        
	        requestLogRepo.save(logEntry);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void logSubEntry(String remarks, String status,
			Long requestId, String department, String office, String userName, LocalDateTime timestamp, String designation) throws Exception {
		try {
			EProposalIncomingRequests logEntry = new EProposalIncomingRequests();

	        logEntry.setRemarks(remarks);
	        logEntry.setStatus(status);
	        logEntry.setRequestId(requestId);
	        //logEntry.setUserCode(Long.parseLong(userCode));

	        logEntry.setTimestamp(timestamp); // Make sure it's in ISO-8601 format
	        
	        logEntry.setUserName(userName);
	        logEntry.setUserDepartment(department);
	        logEntry.setUserOffice(office);
	        logEntry.setUserDesignation(designation);
	        logEntry.setEntrydate(LocalDateTime.now());
	        logEntry.setType("Status Update");
	        
	        requestLogRepo.save(logEntry);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Optional<EProposalRequest> getRequestById(Long requestId){
		try {
			return eProposalRequestRepo.findById(requestId);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public List<EProposalIncomingRequests> getEPSStatus(Long requestId){
		try {
			return epsIncomingReqRepo.findByRequestIdOrderByTimestampAsc(requestId);
		}catch(Exception ex) {
			throw ex;
		}
	}
}
