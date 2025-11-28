package sad.storereg.services.appdata;

import static sad.storereg.models.auth.Role.ADMIN;
import static sad.storereg.models.auth.Role.CH;
import static sad.storereg.models.auth.Role.USER;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsHistory;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationFlow;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.ApplicationFlowRepository;

@Service
@RequiredArgsConstructor
public class DecisionServices {

	private final ApplicationsRepository appRepo;
	private final ApplicationFlowRepository appFlowRepo;
	private final AllotmentServices allotmentService;
	private final ApplicationHistoryServices appHistoryService;
	private final ServiceNotification serviceNotification;
	private final FormServices formService;	
	
	@Transactional
	public ApplicationsHistory cancel(String appNo, String remarks, Long userCode){
		try {
	
			Applications app = appRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			//if(!(app.getUsername().equals(username)))
			//	throw new UnauthorizedException("Application does not belong to user");
			
			if(!(app.getUserCode().equals(userCode)))
					throw new UnauthorizedException("Application does not belong to user");
			
			if(app.getAppStatus()>2 && app.getAppStatus()!=21)
				throw new UnauthorizedException("Application cannot be cancelled");
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), 7);
			
			if(app.getAppStatus()==2) {
				allotmentService.rejectApplication(appNo, remarks);
			}
			
			app.setAppStatus(7);
			app.setLevel(-1);
			appRepo.save(app);
			
			return appHistoryService.save(app.getAppNo(),remarks, new Date(), userCode, appFlow.getCode());
			
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to cancel application", e);
		}
	}
	
	@Transactional
	public ApplicationsHistory discard(String appNo, String remarks, Long userCode){
		try {
	
			Applications app = appRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			if(!(app.getUserCode().equals(userCode)))
				throw new UnauthorizedException("Application does not belong to user");
			
			if(app.getAppStatus()!=0)
				throw new UnauthorizedException("Application cannot be discarded");
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), 8);
			
			app.setAppStatus(8);
			app.setLevel(-1);
			appRepo.save(app);
			
			return appHistoryService.save(app.getAppNo(), remarks, new Date(), userCode, appFlow.getCode());
			
			//ApplicationsHistory discarded = ApplicationsHistory.builder().appNo(app.getAppNo()).entrydate(new Date()).remarks(remarks).username(username)
			//		.flowCode(appFlow.getCode()).build();
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to discard application", e);
		}	
	}
	
	//Forward application from DA to CH - no longer required
//	@Transactional
//	public ResponseEntity<Map<String, String>> forward(ActionRequest request, User user){
//		try {
//			Map<String, String> responseMap = new HashMap<>();
//			
//			if(user.getRole().equals(USER) || user.getRole().equals(ADMIN) || user.getRole().equals(CS)) {
//    			responseMap.put("detail", "Action not allowed");
//    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
//    		}
//	
//			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
//			
//			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
//			
//			app.setAppStatus(request.getActionCode());
//			app.setLevel(appFlow.getNextLevel());
//			appRepo.save(app);
//			
//			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getUsername(), appFlow.getCode());
//			
//			responseMap.put("detail", "Application Forwarded to Chairman");
//			return new ResponseEntity<>(responseMap, HttpStatus.OK);
//		
//		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
//			throw ex;
//		} catch (Exception e) {
//			throw new InternalServerError("Failed to forward application", e);
//		}	
//	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> submitForm(ActionRequest request, User user){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(!user.getRole().equals(USER)) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
	
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			
			responseMap.put("detail", "Application Forwarded");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to forward application", e);
		}	
	}

	@Transactional
	public ResponseEntity<Map<String, String>> formReUpload(ActionRequest request, User user){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(user.getRole().equals(USER) || user.getRole().equals(ADMIN)) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
	
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			
			responseMap.put("detail", "Application sent back to applicant");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to perform action", e);
		}	
	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> forwardToCS(ActionRequest request, User user){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(!user.getRole().equals(CH)) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
			
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			if(!(app.getAppStatus()==3 || app.getAppStatus()==11 || app.getAppStatus()==10)){
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
			
			//if(app.getAppStatus()==10 || app.getAppStatus()==11)
			//	throw new UnauthorizedException("Not coded yet");
				
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			
			//allotmentService.allotmentRequest(app, request, user);
			
			responseMap.put("detail", "Application Forwarded to CS for approval");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to forward application", e);
		}	
	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> allot(ActionRequest request, User user, HttpServletRequest httpRequest){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(!user.getRole().equals(CH)) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
	
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			
			allotmentService.allot(app, user, LocalDateTime.now(), request.getLetterNo());
			serviceNotification.sendSms(request.getActionCode(), request.getAppNo(), user.getUsername(), httpRequest);
			responseMap.put("detail", "Allotment order sent to applicant");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to upload allotment order", e);
		}	
	}
	
//	@Transactional
//	public ResponseEntity<Map<String, String>> csApprove2(ActionRequest request, User user){
//		try {
//			Map<String, String> responseMap = new HashMap<>();
//			
//			if(!user.getRole().equals(CS)) {
//    			responseMap.put("detail", "Action not allowed");
//    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
//    		}
//	
//			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
//			
//			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
//			
//			app.setAppStatus(request.getActionCode());
//			app.setLevel(appFlow.getNextLevel());
//			appRepo.save(app);
//			
//			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getUsername(), appFlow.getCode());
//			
//			allotmentService.csApprove(request, user);
//			
//			responseMap.put("detail", "Allotment is approved by CS");
//			return new ResponseEntity<>(responseMap, HttpStatus.OK);
//		
//		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
//			throw ex;
//		} catch (Exception e) {
//			throw new InternalServerError("Failed to approve allotment", e);
//		}	
//	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> requestChangeWL(ActionRequest request, User user){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(!(user.getRole().equals(USER))) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
			if(user.getRole().equals(USER) && (request.getReason()==null || request.getRemarks()==null)){
    			responseMap.put("detail", "reason and remarks is required");
    			return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
    		}
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), user.getRole().equals(USER)?request.getReason()+": "+request.getRemarks():request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			
			//Quarters Change the temporary shit to 0
			if(request.getActionCode()==11) 
				allotmentService.csSendBack(request,user);
			
			if(request.getActionCode()==10) 
				allotmentService.denyAllotment(app.getAppNo(), app.getAllotmentId());			
			
			responseMap.put("detail", "Request sent");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to make request", e);
		}	
	}
	
	@Transactional
	public ResponseEntity<Map<String, String>> acceptAllotment(ActionRequest request, User user){
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(!user.getRole().equals(USER) ) {
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
	
			Applications app = appRepo.findByAppNo(request.getAppNo()).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			if(app.getAppStatus()!=4){
    			responseMap.put("detail", "Action not allowed");
    			return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
    		}
			
			//if(request.getOccupationDate()==null) {
			//	responseMap.put("detail", "Occupation date is required");
    		//	return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
			//}
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), request.getActionCode());
			
			app.setAppStatus(request.getActionCode());
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(request.getAppNo(), request.getRemarks(), new Date(), user.getId(), appFlow.getCode());
			allotmentService.acceptAllotment(request.getOccupationDate(), app);
			responseMap.put("detail", "Application Forwarded");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to perform action", e);
		}	
	}
	
	@Transactional
	public Map<String, String> allotEP(String appNo, Integer actionCode, MultipartFile file, LocalDateTime timestamp, String letterNo, String remarks, HttpServletRequest httpRequest){
		try {
			Map<String, String> responseMap = new HashMap<>();

			Applications app = appRepo.findByAppNo(appNo).orElseThrow(()->new ObjectNotFoundException("Invalid Application no."));
			
			ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(app.getAppStatus(), actionCode);
			
			app.setAppStatus(actionCode);
			app.setLevel(appFlow.getNextLevel());
			appRepo.save(app);
			
			appHistoryService.save(appNo, remarks, new Date(), null, appFlow.getCode());
			
			if(actionCode==4) {
				allotmentService.allot(app, null, timestamp, letterNo);
				serviceNotification.sendSms(actionCode, appNo, "", httpRequest);
				responseMap.put("detail", "Allotment order sent to applicant");
			}
			else if(actionCode==3) {
				allotmentService.rejectAllotEP(app, timestamp);
			}
			Map<String,String> resp = formService.uploadOrder2(file, appNo);
			responseMap.put("formCode", resp.get("formCode"));
			allotmentService.setOrderName(app, resp.get("formPath"));
			responseMap.put("detail", "Rejected");
			return responseMap;
		
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed approve allotment", e);
		}	
	}
}
