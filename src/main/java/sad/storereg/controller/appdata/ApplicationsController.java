package sad.storereg.controller.appdata;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsHistory;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.ApplicationsServices;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationsController {
	
	private final ApplicationsServices appService;

	@Auditable
	@PostMapping(consumes = "application/json")
	public ResponseEntity<Map<String, String>> createUpdateApplicationDraft(@Valid @RequestBody Applications application, @AuthenticationPrincipal User user) {
		
		return appService.createUpdateApplicationDraft(application, user.getId(), 0);
		
	}
	
	@PostMapping(path="/all",params = { "page", "size" })
    public Page<Applications> getAllApplications(@RequestBody MyFilter filter,
            @RequestParam("page") final int page,
            @RequestParam("size") final int size) {
        return appService.getAllApplications( page, size, filter);
    }
	
	@GetMapping(params = { "page", "size" })
    public Page<Applications> getApplicationsWithActions(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
        return appService.getApplications(user, page, size);
    }
	
	@GetMapping(path="/qc", params = { "page", "size" })
    public Page<Applications> getQuarterChangeRequestApplications(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
        return appService.getQCApplications(user, page, size);
    }
	
	@GetMapping(path="/bck", params = { "page", "size" })
    public Page<Applications> getReturnedApplications(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
        return appService.getReturnedApplications(user, page, size);
    }

	@Auditable
	@Transactional
	@PostMapping("/upload")
	public ResponseEntity<Map<String, String>> performAction(HttpServletRequest headerRequest,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("applicationNo") String applicationNo,
			@AuthenticationPrincipal User user) {
			Map<String, String> responseMap = new HashMap<>();

			if(file==null || file.isEmpty()) {
				responseMap.put("detail","File is empty");
				return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
			}			
			return appService.uploadForm(applicationNo, file, user);
	}
	
	@Auditable
	@Transactional
	@PostMapping("/action")
	public ResponseEntity<Map<String, String>> performAction(@RequestBody ActionRequest request, HttpServletRequest httpRequest,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@AuthenticationPrincipal User user) {
		try {
			Map<String, String> responseMap = new HashMap<>();
			
			if(request.getActionCode()==3 || request.getActionCode()==4 || request.getActionCode()==5 || request.getActionCode()==6 || request.getActionCode()==2 || request.getActionCode()==9
					|| request.getActionCode()==10  || request.getActionCode()==13 || request.getActionCode()==12 || request.getActionCode()==14 
					|| request.getActionCode()==15) {
				return appService.action(request, user, httpRequest);
			}
			
			//Discard Draft by User
			if(request.getActionCode()==8) {
				ApplicationsHistory c = appService.discardDraft(request, user);
				
				if (c != null) {
					responseMap.put("detail", "Application discarded successfully");
					return new ResponseEntity<>(responseMap, HttpStatus.OK);
				}
				else {
					responseMap.put("detail","Unable to discard application");
					return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
	
			//For Cancellation by applicant
			if(request.getActionCode()==7) {
			
				ApplicationsHistory c = appService.cancelApplication(request, user);
				
				//Here is application status is 3 or 4 require to delete from waiting list
				
				if (c != null) {
					responseMap.put("detail", "Application cancelled successfully");
					return new ResponseEntity<>(responseMap, HttpStatus.OK);
				}
				else {
					responseMap.put("detail","Unable to cancel application");
					return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			
			else {
				responseMap.put("detail","Action not coded yet from backend");
				return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
			}
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		} catch (Exception e) {
			throw new InternalServerError("Failed to perform action", e);
		}
	}
	
	@GetMapping(path="/summary")
    public ResponseEntity<Map<String,Object>> getSummary(@AuthenticationPrincipal User user) {
		
		Map<String, Object> responseMap = appService.getSummary(user.getId());
        
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }
	
	@Auditable
	@PostMapping(path="/generate", consumes = "application/json")
	public void generateForm(@Valid @RequestBody Applications application, HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException, JRException {
		try {
			
			if(application.getEntrydate()!=null)
				throw new UnauthorizedException("Form already generated. Go to Download");
			
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			ResponseEntity<Map<String, String>> responseMap = appService.createUpdateApplicationDraft(application, user.getId(), 0);
			String headerValue = "attachment; filename=GADB_"+responseMap.getBody().get("applicationNo")+ "_" +new Date() + ".pdf";
			response.setHeader(headerKey, headerValue);
			
			appService.generateForm(response, responseMap.getBody().get("applicationNo"), user);
			
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@GetMapping(path = "/download/{applicationNo}")
	public void downloadForm(@PathVariable String applicationNo, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {
		
		try {			
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=GADB_" +applicationNo+"_"+ new Date() + ".pdf";
			response.setHeader(headerKey, headerValue);
			appService.generateForm(response, applicationNo, user);
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@Auditable
	@PostMapping(path="/esign", consumes = "application/json")
	public void eSign(@Valid @RequestBody Applications application, HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException, JRException {
		try {
			
			if(application.getEntrydate()!=null)
				throw new UnauthorizedException("Form already generated. Go to Download");
			
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			ResponseEntity<Map<String, String>> responseMap = appService.createUpdateApplicationDraft(application, user.getId(), 0);
			String headerValue = "attachment; filename=GADB_"+responseMap.getBody().get("applicationNo")+ "_" +new Date() + ".pdf";
			response.setHeader(headerKey, headerValue);
			
			appService.generateForm(response, responseMap.getBody().get("applicationNo"), user);
			
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@GetMapping(path = "/remarks/{applicationNo}")
	public ResponseEntity<Map<String, String>> getRemarks(@PathVariable String applicationNo, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {
		
		try {						
			return appService.getRemarks(applicationNo, user);
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@GetMapping(path = "/remarks-da/{applicationNo}")
	public ResponseEntity<Map<String, String>> getRemarksDA(@PathVariable String applicationNo, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {
		
		try {						
			return appService.getRemarksDA(applicationNo, user);
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			System.out.println(ex);
			throw ex;
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping("/upload-approval-order")
	public ResponseEntity<Map<String, String>> approvalOrderUpload(HttpServletRequest headerRequest,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("applicationNo") String applicationNo,
			@AuthenticationPrincipal User user) {
			Map<String, String> responseMap = new HashMap<>();

			if(file==null || file.isEmpty()) {
				responseMap.put("detail","File is empty");
				return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
			}			
			return appService.approvalOrderUpload(applicationNo, file, user);
	}
	
	
}
