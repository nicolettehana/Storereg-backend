package sad.storereg.controller.appdata;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.DocDTO;
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.dto.appdata.VacateRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.VacateRequest;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.AllotmentServices;
import sad.storereg.services.appdata.QuartersServices;
import sad.storereg.services.appdata.VacateDocumentService;
import sad.storereg.services.appdata.VacateRequestServices;

@RestController
@RequestMapping("/vacate-request")
@RequiredArgsConstructor
public class VacateRequestController {
	
	private final VacateRequestServices vacateReqService;
	private final QuartersServices quartersService;
	private final AllotmentServices allotmentService;
	
	@GetMapping(params = { "page", "size" })
    public Page<VacateRequestDTO> getAllVacateRequests(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
		try {
			return vacateReqService.getVacateRequests(user, page, size);
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to fetch vacate requests",ex);
		}
    }
	
	@GetMapping(path="/pending", params = { "page", "size" })
    public Page<VacateRequestDTO> getPendingVacateRequests(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
		try {
			return vacateReqService.getVacateRequests("P",user, page, size);
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to fetch vacate requests",ex);
		}
    }
	
	@GetMapping(path="/completed", params = { "page", "size" })
    public Page<VacateRequestDTO> getCompletedVacateRequests(
            @RequestParam("page") final int page,
            @RequestParam("size") final int size,
            @AuthenticationPrincipal User user) {
		try {
			return vacateReqService.getVacateRequests("C",user, page, size);
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to fetch vacate requests",ex);
		}
    }
	
	@GetMapping("/get-available-quarters")
	public List<QuartersDTO> getListOfOccupiedQuarters(@AuthenticationPrincipal User user){
		try {
			return quartersService.getQuartersList(user);
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to get quarters list",ex);
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping
	public ResponseEntity<Map<String,String>> addVacateRequest(HttpServletRequest headerRequest, 
			@RequestBody VacateRequestDTO vacateRequest,
			@AuthenticationPrincipal User user){
		try {

			Map<String,String> response = new HashMap<>();
			vacateRequest.setStatus("P");
			vacateReqService.createVacateRequest(vacateRequest, user.getUsername());
			
			response.put("detail", "Vacate Request Submitted successfully");
			return new ResponseEntity<>(response, HttpStatus.CREATED);	
			
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to make vacate request",ex);
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping("/reject")
	public ResponseEntity<Map<String,String>> rejectVacateRequest(
		    @RequestBody VacateRequestDTO request) throws BadRequestException{
		try {
			Map<String,String> response = new HashMap<>();
			vacateReqService.rejectVacateRequest(request);
			
			response.put("detail", "Vacate Request Rejected successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);	
			
		}catch (ObjectNotFoundException | UnauthorizedException |BadRequestException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to reject vacate request",ex);
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping("/accept")
	public ResponseEntity<Map<String,String>> acceptVacateRequest(
		    @RequestBody VacateRequestDTO request, 
		    @AuthenticationPrincipal User user) throws BadRequestException{
		try {
			Map<String,String> response = new HashMap<>();
			vacateReqService.acceptVacateRequest(request, user.getUsername());
			
			response.put("detail", "Vacate Request Accepted successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);	
			
		}catch (ObjectNotFoundException | UnauthorizedException |BadRequestException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to accept vacate request",ex);
		}
	}
	
	@GetMapping("/stats")
	public ResponseEntity<Map<String, String>> getStats(){
		try {
			Map<String, String> responseData = new HashMap<>();
			responseData.put("pendingAllotments", allotmentService.getNoOfPendingAllotments()+"");
			responseData.put("vacateRequests", vacateReqService.getNoOfVacateRequests()+"");
			return new ResponseEntity<>(responseData, HttpStatus.OK);
		}catch(Exception ex) {
			throw ex;
		}
	}

}
