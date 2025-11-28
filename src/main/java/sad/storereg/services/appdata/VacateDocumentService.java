package sad.storereg.services.appdata;

import static sad.storereg.models.auth.Role.USER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.DocDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.VacateDocument;
import sad.storereg.models.appdata.VacateRequest;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.VacateDocumentRepository;
import sad.storereg.repo.appdata.VacateRequestRepository;

@Service
@RequiredArgsConstructor
public class VacateDocumentService {
	
	private final VacateRequestRepository vacateRequestRepo;
	private final QuartersServices quartersService;
	private final ReportsService reportsService;
	private final ApplicationsServices appService;
	private final CoreServices coreService;
	private final FormServices formService;
	private final VacateDocumentRepository vacateDocRepo;
	
	public List<VacateDocument> getVacateDocuments(String quarterNo){
		try {
			Integer allotmentId = quartersService.getQuarter(quarterNo).getAllotmentId();
			
			return vacateRequestRepo.findByAllotment_Id(allotmentId).getDocuments();
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public void getDocument(HttpServletResponse response, UUID documentCode, User user) {
    	try {    			
    		
    		//if(user.getRole().equals(USER) && !(user.getUsername().equals(application.getUsername())))
    		//	throw new UnauthorizedException("Application does not belong to user");

    		reportsService.downloadDocument(response, documentCode);

    	}
    	catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
    		System.out.println(ex.getLocalizedMessage());
    		throw ex;
    	} catch (Exception e) {
    		throw new InternalServerError("Unable to fetch document", e);
    	}
    }
	
	@Transactional
    public Map<String, String> uploadDocument(String quarterNo, String documentType, MultipartFile file, User user) {
    	
    	try {
	    	Quarters quarter = quartersService.getQuarter(quarterNo);
	    	System.out.println("Quarter: "+quarter);
	    	Optional<Applications> app = appService.getApplication(quarter.getAllotmentId().longValue());
	    	
	    	if(app.isEmpty())
	    		throw new ObjectNotFoundException("No application associated with quarter no.");
	    	
	    	//if((user.getRole().equals(USER)) && !(user.getUsername().equals(app.get().getUsername())))
	    	//	throw new UnauthorizedException("User not authorised");
	    	
	    	if((user.getRole().equals(USER)) && !(user.getId().equals(app.get().getUserCode())))
		    		throw new UnauthorizedException("User not authorised");
	    	
	    	Map<String,String> responseMap = new HashMap<>();
	    	
	    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    	Map<String, Object> data = coreService.isValidFilename(originalFileName);
	    	
	    	if (!(Boolean)data.get("status")) {
	    		responseMap.put("message", "Invalid Filname/Length of Filename");
	    		throw new InternalServerError("Invalid Filename/Length of Filename");
	    	}
	    	
	    	//Here upload the file in the FormPath
	    	String documentCode = formService.uploadDocument(file, app.get().getAppNo());
	    	
	    	VacateDocument doc = new VacateDocument();
	    	doc.setDocumentCode(UUID.fromString(documentCode));
	    	doc.setDocumentType(documentType);
	    	doc.setUploadedAt(LocalDateTime.now());
	    	vacateDocRepo.save(doc);   	
	    	
	    	Map<String, String> response = new HashMap<>();
	    	response.put("documentCode", documentCode);
	    	response.put("detail", "Successfully uploaded document");
	    	return response;
	    	
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
	
	@Transactional
	public void setVacateRequestId(VacateRequest request, List<DocDTO> docs) {
		try {
			for (DocDTO doc : docs) {
				Optional<VacateDocument> vacateDoc = vacateDocRepo.getByDocumentCode(doc.getDocumentCode());
				//if(vacateDoc.isEmpty())
				//	throw new ObjectNotFoundException("Document wasn't uploaded");
				
				vacateDoc.get().setVacateRequest(request);
				vacateDocRepo.save(vacateDoc.get());
			}
		}catch(Exception ex) {
			throw ex;
		}
	}
}
