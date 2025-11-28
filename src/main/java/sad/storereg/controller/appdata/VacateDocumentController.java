package sad.storereg.controller.appdata;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
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
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.VacateDocument;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.VacateDocumentService;

@RestController
@RequestMapping("/vacate-document")
@RequiredArgsConstructor
public class VacateDocumentController {
	
	private final VacateDocumentService vacateDocService;
	
	@PostMapping
	public ResponseEntity<List<VacateDocument>> getVacateDocuments(@RequestBody QuartersDTO request) {
		try {
			if(request.getQuarterNo()==null)
				throw new BadRequestException("Quarter no. is required");
			
			return new ResponseEntity<>(vacateDocService.getVacateDocuments(request.getQuarterNo()), HttpStatus.OK);
			
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to fetch vacate documents",ex);
		}	
	}
	
	@GetMapping(path = "/{documentCode}")
	public void downloadForm(@PathVariable String documentCode, HttpServletResponse response,
			@AuthenticationPrincipal User user) throws IOException, JRException {
		
		try {			
			response.setContentType("application/pdf");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=VacateDoc_" +documentCode+".pdf";
			response.setHeader(headerKey, headerValue);
			vacateDocService.getDocument(response, UUID.fromString(documentCode), user);
			return;
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping("/upload")
	public ResponseEntity<Map<String, String>> uploadDocument(HttpServletRequest headerRequest,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("quarterNo") String quarterNo,
			@RequestParam("documentType") String documentType,
			@AuthenticationPrincipal User user) {
			Map<String, String> responseMap = new HashMap<>();

			
			if(file==null || file.isEmpty()) {
				responseMap.put("detail","File is empty");
				return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
			}			
			return new ResponseEntity<>(vacateDocService.uploadDocument(quarterNo, documentType, file, user), HttpStatus.OK);
	}

}
