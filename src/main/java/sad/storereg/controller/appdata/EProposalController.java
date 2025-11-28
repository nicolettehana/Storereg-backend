package sad.storereg.controller.appdata;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.EPSDTO;
import sad.storereg.dto.appdata.EPStatusUpdateDTO;
import sad.storereg.dto.appdata.EProposalRequestWrapper;
import sad.storereg.dto.master.QuarterAssetDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.logs.EProposalIncomingRequests;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.EProposalRequest;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationFlow;
import sad.storereg.repo.master.ApplicationFlowRepository;
import sad.storereg.services.appdata.AllotmentServices;
import sad.storereg.services.appdata.ApplicationHistoryServices;
import sad.storereg.services.appdata.ApplicationsServices;
import sad.storereg.services.appdata.EProposalService;
import sad.storereg.services.master.QuartersMasterService;

@RestController
@RequiredArgsConstructor
public class EProposalController {
	
	private final EProposalService eProposalService;
	private final ApplicationsServices appService;
	private final ApplicationHistoryServices appHistoryService;
	private final ApplicationFlowRepository appFlowRepo;
	private final AllotmentServices allotmentService;
	private final QuartersMasterService quartersMasterService;
	
//	@PostMapping(path="/eproposal-status-update", consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
//	//@PostMapping(path="/eproposal-status-update")
//	public ResponseEntity<?> eProposalCallBack(@RequestPart("data") @Valid EProposalDTO dto,
//            @RequestPart(name = "file", required = false) MultipartFile file) {
//		try {
//			System.out.println("data: "+dto);
//			System.out.println("File: " + (file != null ? file.getOriginalFilename() : "No file"));
//			return ResponseEntity.ok("Received successfully");			
//		}
//		catch(Exception ex) {
//			throw ex;
//		}
//	}
	
	@Transactional
	@PostMapping("/eproposal-request")
	public ResponseEntity<?> createEProposalRequest(@RequestBody EProposalRequestWrapper data, @AuthenticationPrincipal User user){
		try {			
			EProposalRequest request= data.getRequest();
			Applications application = data.getApplication();
			Map<String, String> responseData = new HashMap<>();
			eProposalService.validateEProposalRequest(request);
			
			String appNo=null;
			if(request.getRequestType().equals("FOA")) {
				appNo = appService.createUpdateApplicationDraft(application, user.getId(), 8).getBody().get("applicationNo");
				//actionCode=21;
				//apps = appService.getApp(appNo);
			}
			else {
				if(application.getAppNo()==null)
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appNo is required");
				appNo = application.getAppNo();
				//apps = appService.validateStatus(appNo, request.getRequestType());
				//if(request.getRequestType().equals("WLA"))
				//	actionCode=22;
				//else 
					if(request.getRequestType().equals("AA")) {
					//actionCode=23;
					allotmentService.createAllotment(application.getAppNo(), user, 1, request.getQuarterNo(), request.getLetterNo());
				}
			}
			request.setAppNo(appNo);
			EProposalRequest ePRequest = eProposalService.createRequest(request);			
			
			responseData.put("requestId", ePRequest.getId().toString());
			responseData.put("appNo", appNo);
			responseData.put("detail", "Success");
			return ResponseEntity.ok(responseData);
		}catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
	}	

	@Transactional
	@PostMapping("/eproposal-insert-allotment-status")
	public ResponseEntity<?> updateApplicationStatusEProposalRequest(@RequestBody EPSDTO request, @AuthenticationPrincipal User user)
	{
		try {
			String appNo=null;
			Integer actionCode=null;
			Applications apps=null;
			
			Optional<EProposalRequest> data = eProposalService.getRequestById(request.getRequestId());
			if(data.isPresent()) {
				if(request.getStatus().equals("S")) {
					apps = appService.getApp(data.get().getAppNo());
					actionCode=(data.get().getRequestType().equals("FOA")?21:(data.get().getRequestType().equals("WLA")?22:23));
					ApplicationFlow appFlow = appFlowRepo.findByFromStatusEqualsAndToStatusEquals(apps.getAppStatus(), actionCode);
					Applications app = appService.setLevelAndStatus(6, actionCode, data.get().getAppNo());
					appHistoryService.save(app.getAppNo(), null, new Date(), user.getId(), appFlow.getCode());
				}
			}
			else {
				
			}
			Map<String, String> responseData = new HashMap<>();
			responseData.put("detail", "Success");
			return ResponseEntity.ok(responseData);
			
		}catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
	}
	
    @PostMapping(path = "/api/eproposal-final-status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> eProposalCallBack(@RequestPart(name="remarks", required=false) String remarks,
    		@RequestPart("status") String status, @RequestPart("requestId") String requestId,
    		@RequestPart(name="userCode", required =false) String userCode, @RequestPart("timestamp") String timestamp,
    		@RequestPart(name="letterNo", required=false) String letterNo, @RequestPart(name="memoNo", required=false) String memoNo,
    		@RequestPart(name="userName", required=false) String userName, @RequestPart(name="userDepartment", required=false) String userDepartment,
    		@RequestPart(name="userDesignation", required=false) String userOffice,
            @RequestPart(name = "signedPdf", required = false) MultipartFile file, @AuthenticationPrincipal User user, HttpServletRequest httpRequest) throws Exception {

        try {
        	
            eProposalService.validateInput(requestId, status, userCode,timestamp, remarks, file, letterNo);
            eProposalService.logEntry(remarks, status,requestId, letterNo, memoNo,
            		timestamp);
            eProposalService.processCallback(remarks, status, requestId, userCode, timestamp, letterNo, memoNo, userName,
            		userDepartment, userOffice, file, user, httpRequest);
            //Upload file first
            //Then update the eproposal request table
            //then update the action stuffs
            
            return ResponseEntity.ok("Received successfully");
        }catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
        	throw ex;
            //return ResponseEntity.badRequest().body("Failed to parse request");
        }
    }
    
    
    
    @PostMapping(path = "/api/eproposal-status-update")
    public ResponseEntity<?> eProposalStatusUpdate(@RequestBody @Valid EPStatusUpdateDTO request, @AuthenticationPrincipal User user, HttpServletRequest httpRequest) throws Exception {

        try {
        	System.out.println("Received: "+request);
        	eProposalService.logSubEntry(request.getFileMovementNote(), request.getStatus(), request.getRequestId(), request.getDepartment(), request.getOffice(), request.getUserName(), request.getMovementDateTime(), request.getDesignation());
            
            return ResponseEntity.ok("updated Successfully");
        }catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
        	throw ex;
            //return ResponseEntity.badRequest().body("Failed to parse request");
        }
    }
    
    @PostMapping(path="/api", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> unifiedApiHandler(
            @RequestPart(name = "functionName", required = true) String functionName,
            @RequestPart(name = "remarks", required = false) String remarks,
            @RequestPart(name = "status", required = false) String status,
            @RequestPart(name = "requestId", required = false) String requestId,
            @RequestPart(name = "userCode", required = false) String userCode,
            @RequestPart(name = "timestamp", required = false) String timestamp,
            @RequestPart(name = "letterNo", required = false) String letterNo,
            @RequestPart(name = "memoNo", required = false) String memoNo,
            @RequestPart(name = "userName", required = false) String userName,
            @RequestPart(name = "department", required = false) String department,
            @RequestPart(name = "designation", required = false) String designation,
            @RequestPart(name = "office", required = false) String office,
            @RequestPart(name = "fileMovementNote", required = false) String fileMovementNote,
            @RequestPart(name = "signedPdf", required = false) MultipartFile file,
            @AuthenticationPrincipal User user, HttpServletRequest httpRequest) {

        try {           
            switch (functionName) {
                case "assetInfo":
                    List<QuarterAssetDTO> result = quartersMasterService.getAllQuarterAssetInfo();
                    return new ResponseEntity<>(result, HttpStatus.OK);

                case "statusUpdate":
                    eProposalService.validateStatusUpdate(requestId, status, fileMovementNote, department, designation, office, userName, timestamp);
                    eProposalService.logSubEntry(fileMovementNote, status, Long.parseLong(requestId), department, office, userName, LocalDateTime.parse(timestamp), designation);
                    
                    return ResponseEntity.ok("Updated Successfully");

                case "finalStatus":
                	
                    eProposalService.validateInput(requestId, status, userCode, timestamp, remarks, file, letterNo);
                    eProposalService.logEntry(remarks, status, requestId, letterNo, memoNo, timestamp);
                    eProposalService.processCallback(remarks, status, requestId, userCode, timestamp,
                            letterNo, memoNo, userName, department, office, file, user, httpRequest);
                    return ResponseEntity.ok("Received successfully");

                default:
                    return ResponseEntity.badRequest().body("Invalid or missing functionName");
            }

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            throw new InternalServerError("Failed to process unified API request", ex);
        }
    }
    
    @GetMapping(path = "/eproposal-get-status/{appNo}")
    public ResponseEntity<List<EProposalIncomingRequests>> eProposalGetStatus(
            @PathVariable("appNo") String appNo,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) throws Exception {

        try {
        	
            return ResponseEntity.ok(eProposalService.getEPSStatus((long) 88));
        } catch (Exception ex) {
        	throw ex;
            //return ResponseEntity.badRequest().body("Failed to parse request");
        }
    }
}