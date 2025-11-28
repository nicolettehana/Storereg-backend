package sad.storereg.controller.appdata;

import static sad.storereg.models.auth.Role.CH;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.apache.coyote.BadRequestException;
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
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.ActionRequest;
import sad.storereg.dto.appdata.AllotmentOrderDownloadDTO;
import sad.storereg.dto.appdata.ApplicantDecisionDTO;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.dto.appdata.PendingAllotmentsDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.AllotmentServices;

@RestController
@RequestMapping("/allotment")
@RequiredArgsConstructor
public class AllotmentController {

	private final AllotmentServices allotmentService;

	@Auditable
	@PostMapping(path = "/order")
	public void downloadAllotmentOrder(@RequestBody ActionRequest request, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {

		try {
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=GADB_Allotment_" + request.getLetterNo() + "_" + new Date()
					+ ".pdf";
			response.setHeader(headerKey, headerValue);
			if (user.getRole().equals(CH))
				allotmentService.getAllotmentOrder(response, request, user, 1);
			else
				allotmentService.getAllotmentOrder(response, request, user, 0);
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}

	@Auditable
	@Transactional
	@PostMapping("/order-upload")
	public void performAction(HttpServletRequest headerRequest, HttpServletResponse response,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("applicationNo") String applicationNo, @RequestParam("letterNo") String letterNo,
			@RequestParam("memoNo") String memoNo, @RequestParam("quarterNo") String quarterNo,
			@AuthenticationPrincipal User user) {

		response.setContentType("application/pdf");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=GADB_Allotment_" + letterNo + "_" + new Date() + ".pdf";
		response.setHeader(headerKey, headerValue);

		if (file == null || file.isEmpty()) {
			throw new InternalServerError("File is empty");
		}
		try {
			allotmentService.uploadOrder(response, applicationNo, letterNo, memoNo, quarterNo, file, user);

		} catch (Exception e) {
			throw new InternalServerError("Unable to upload order", e);
		}
	}

	@Auditable
	@Transactional
	@PostMapping("/order-final-upload")
	public void finalUpload(HttpServletRequest headerRequest, HttpServletResponse response,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("applicationNo") String applicationNo, @RequestParam("letterNo") String letterNo,
			@RequestParam("memoNo") String memoNo, @AuthenticationPrincipal User user) {

		response.setContentType("application/pdf");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=GADB_Allotment_" + letterNo + "_" + new Date() + ".pdf";
		response.setHeader(headerKey, headerValue);

		if (file == null || file.isEmpty()) {
			throw new InternalServerError("File is empty");
		}
		try {
			allotmentService.uploadFinalOrder(response, applicationNo, letterNo, memoNo, file, user);
		} catch (Exception e) {
			throw new InternalServerError("Unable to final upload order", e);
		}
	}

	@Auditable
	@Transactional
	@PostMapping("/order-cs-upload")
	public void csUpload(HttpServletRequest headerRequest, HttpServletResponse response,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("applicationNo") String applicationNo, @RequestParam("letterNo") String letterNo,
			@RequestParam("memoNo") String memoNo, @AuthenticationPrincipal User user) {

		response.setContentType("application/pdf");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=GADB_Allotment_" + letterNo + "_" + new Date() + ".pdf";
		response.setHeader(headerKey, headerValue);

		if (file == null || file.isEmpty()) {
			throw new InternalServerError("File is empty");
		}
		try {
			allotmentService.uploadCSOrder(response, applicationNo, letterNo, memoNo, file, user);
		} catch (Exception e) {
			throw new InternalServerError("Unable to final upload order", e);
		}
	}

	@GetMapping(path = "/{applicationNo}")
	public ResponseEntity<Map<String, Object>> allotmentDetails(@PathVariable String applicationNo,
			HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException, JRException {
		try {
			Map<String, Object> responseMap = allotmentService.getAllotmentDetails(applicationNo);

			return new ResponseEntity<>(responseMap, HttpStatus.OK);

		}
		catch (Exception ex) {
			throw new InternalServerError("Unable to fetch user information", ex);
		}
	}

	@GetMapping(path = "/request/{applicationNo}")
	public ResponseEntity<Map<String, Object>> getAllotmentRequestDetails(@PathVariable String applicationNo) {
		try {
			return new ResponseEntity<>(allotmentService.getAllotmentRequestDetails(applicationNo), HttpStatus.OK);

		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch information", ex);
		}
	}

	@GetMapping(path = "/pending", params = { "page", "size" })
	public Page<PendingAllotmentsDTO> getPendingAllotments(@RequestParam("page") final int page,
			@RequestParam("size") final int size) {
		return allotmentService.getPendingAllotments(page, size);
	}

	@PostMapping(path = "/completed", params = { "page", "size" })
	public Page<PendingAllotmentsDTO> getCompletedAllotments(@RequestParam("page") final int page,
			@RequestParam("size") final int size, @RequestBody(required = false) MyFilter filter) {
		return allotmentService.getCompletedAllotments(page, size, filter);
	}

	@PostMapping(path = "/letter")
	public void downloadFileAllotmentOrder(@RequestBody AllotmentOrderDownloadDTO request, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {

		try {
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=GADB_Allotment_" + request.getLetterNo() + ".pdf";
			response.setHeader(headerKey, headerValue);
			allotmentService.getFileAllotmentOrder(response, request, user, 0);
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}

	@Auditable
	@Transactional
	@PostMapping("/upload-decision-letter")
	public void uploadAcceptanceLetter(HttpServletRequest headerRequest, HttpServletResponse response,
			@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("code") char code,
			@RequestParam("applicationNo") String applicationNo, @AuthenticationPrincipal User user) {

		response.setContentType("application/pdf");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=GADB_Acceptance_" + applicationNo + "_" + new Date() + ".pdf";
		response.setHeader(headerKey, headerValue);

		if (file == null || file.isEmpty()) {
			throw new InternalServerError("File is empty");
		}
		try {
			allotmentService.uploadAcceptanceLetter(response, applicationNo, code, file, user);
		} catch (Exception e) {
			throw new InternalServerError("Unable to final upload order", e);
		}
	}

	@Auditable
	@Transactional
	@PostMapping(path = "/occupy")
	public ResponseEntity<Map<String, String>> occupy(@RequestBody ActionRequest request) {
		try {
			Map<String, String> response = new HashMap<>();

			if (request.getAppNo() == null || request.getOccupationDate() == null || request.getAppNo().length() == 0)
				throw new BadRequestException("Invalid input provided");

			allotmentService.markAsOccupied(request.getOccupationDate(), request.getAppNo());
			response.put("detail", "Quarter marked as occupied");

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			throw new InternalServerError("Unable to perform action", ex);
		}
	}

	@PostMapping(path = "/get-applicant-letter")
	public void downloadApplicantLetter(@RequestBody AllotmentOrderDownloadDTO request, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {

		try {
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=GADB_Acceptance_" + request.getAppNo() + ".pdf";
			response.setHeader(headerKey, headerValue);
			allotmentService.getApplicantLetter(response, request, user);
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping(path = "/cancel")
	public ResponseEntity<Map<String, String>> cancelAllotment(@RequestBody ActionRequest request, @AuthenticationPrincipal User user) {
		try {
			Map<String, String> response = new HashMap<>();

			allotmentService.cancelAllotment(request.getAppNo(), user.getId(), request.getRemarks());

			response.put("detail", "Allotment cancelled");

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			throw new InternalServerError("Unable to perform action", ex);
		}
	}

	
}
