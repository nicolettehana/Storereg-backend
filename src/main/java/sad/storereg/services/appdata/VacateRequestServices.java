package sad.storereg.services.appdata;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.DocDTO;
import sad.storereg.dto.appdata.VacateRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.VacateRequest;
import sad.storereg.models.auth.User;
import sad.storereg.repo.appdata.VacateRequestRepository;

@Service
@RequiredArgsConstructor
public class VacateRequestServices {

	private final VacateRequestRepository vacateRequestRepository;
	private final QuartersServices quarterServices;
	private final VacateDocumentService vacateDocServices;
	private final ApplicationsServices appServices;
	private final AllotmentServices allotmentService;
	private final VacatedServices vacatedService;
	
	@Transactional
	public VacateRequest createVacateRequest(VacateRequestDTO request, String username) {
		try {
			VacateRequest vacateRequest = new VacateRequest();
			vacateRequest.setAllotment(quarterServices.getAllotment(request.getQuarterNo()));
			vacateRequest.setRequestDate(LocalDateTime.now());
			vacateRequest.setStatus(request.getStatus());
			vacateRequest.setVacateDate(request.getVacateDate());
			vacateRequest.setUsername(username);
			if(request.getStatus().equals("A"))
					vacateRequest.setApprovedDate(new Date());
			vacateRequestRepository.save(vacateRequest);
			
			vacateDocServices.setVacateRequestId(vacateRequest, request.getDocuments());
			
			appServices.setVacateStatus(17, vacateRequest.getAllotment().getId());
			return null;
		}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<VacateRequest> getAllRequests() {
        return vacateRequestRepository.findAll();
    }
	
	public VacateRequest getRequestById(Long id) {
        return vacateRequestRepository.findById(id).orElseThrow(()->new ObjectNotFoundException("Vacate Request not found"));
    }
	
	public VacateRequest updateStatus(Long id, String status) {
        return vacateRequestRepository.findById(id)
                .map(request -> {
                    request.setStatus(status);
                    return vacateRequestRepository.save(request);
                }).orElseThrow(() -> new RuntimeException("Vacate Request not found!"));
    }
	
	public Page<VacateRequestDTO> getVacateRequests(User user, Integer page, Integer size) {
        try {
        	
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

            Page<VacateRequest> pagedVacateRequests = vacateRequestRepository.findAllByUsername(user.getUsername(), pageable);
            
            return getData(pagedVacateRequests);

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch Vacate Requests", e);
        }
    }
	
	
	public Page<VacateRequestDTO> getVacateRequests(String status, User user, Integer page, Integer size) throws BadRequestException {
        try {
        	
        	PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");

        	if(status.equals("P"))
        		return(getData(vacateRequestRepository.findAllByStatusEquals(status, pageable)));
        	else if(status.equals("C")) 
        		return getData(vacateRequestRepository.findAllByStatusNot("P",pageable));
        	else
        		throw new BadRequestException("Invalid Status Code");

        } catch(BadRequestException ex) {
        	throw ex;
        } 
        catch (Exception e) {
            throw new InternalServerError("Unable to fetch pending Vacate Requests", e);
        }
    }
	
	private Page<VacateRequestDTO> getData(Page<VacateRequest> request){
		return request.map(this::convertToDTO);
	}
	
	private VacateRequestDTO convertToDTO(VacateRequest request) {
	    VacateRequestDTO dto = new VacateRequestDTO();
	    
	    //dto.setAppNo(request.getAllotment().getApplicationNo());
	    dto.setQuarterNo(request.getAllotment().getQuarterNo()+" "+quarterServices.getQuarter(request.getAllotment().getQuarterNo()).getQuarterName());
	    dto.setVacateDate(request.getVacateDate());
	    dto.setRequestedDate(request.getRequestDate().toLocalDate());
	    dto.setStatus(request.getStatus().equals("P")?"Pending":request.getStatus().equals("R")?"Rejected":"Accepted");
	    dto.setRemarks(request.getRemarks());
	    dto.setAllotmentCode(request.getAllotment().getId());
	    dto.setDecisionDate(request.getApprovedDate()==null?null:request.getApprovedDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
	    
	    // Convert documents list
//	    List<Map<String, String>> documentList = request.getDocuments().stream()
//	        .map(doc -> Map.of("documentType", doc.getDocumentType(), "documentCode", doc.getDocumentCode().toString()))
//	        .collect(Collectors.toList());
//	    
//	    dto.setDocuments(documentList);
	 // Convert documents list
	    List<DocDTO> documentList = request.getDocuments().stream()
	        .map(doc -> {
	            DocDTO docDTO = new DocDTO();
	            docDTO.setDocumentType(doc.getDocumentType());
	            docDTO.setDocumentCode(doc.getDocumentCode());
	            return docDTO;
	        })
	        .collect(Collectors.toList());

	    dto.setDocuments(documentList);
	    
	    return dto;
	}
	
	public void rejectVacateRequest(VacateRequestDTO request) throws Exception {
		try {
			if(request.getAllotmentCode()==null || request.getRemarks()==null || request.getRemarks().length()==0)
				throw new BadRequestException("Allotment code and remarks are required");
			VacateRequest vacateRequest = vacateRequestRepository.findByAllotment_IdAndStatus(request.getAllotmentCode().intValue(), "P");
			vacateRequest.setRemarks(request.getRemarks());
			vacateRequest.setStatus("R");
			vacateRequest.setApprovedDate(new Date());
			vacateRequestRepository.save(vacateRequest);
			
			appServices.setVacateStatus(19, vacateRequest.getAllotment().getId());
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void acceptVacateRequest(VacateRequestDTO request, String username) throws Exception {
		try {
			if(request.getAllotmentCode()==null || request.getRemarks()==null || request.getRemarks().length()==0)
				throw new BadRequestException("Allotment code and remarks are required");

			VacateRequest vacateRequest = vacateRequestRepository.findByAllotment_IdAndStatus(request.getAllotmentCode().intValue(), "P");
			vacateRequest.setRemarks(request.getRemarks());
			vacateRequest.setStatus("A");
			vacateRequest.setApprovedDate(new Date());
			vacateRequestRepository.save(vacateRequest);
			
			Allotments allotment=allotmentService.vacateAllotment2(request.getAllotmentCode(), vacateRequest.getVacateDate());
			
			Applications app = appServices.setVacateStatus(18, vacateRequest.getAllotment().getId());
			
			vacatedService.createVacated(vacateRequest.getVacateDate(), username, allotment.getQuarterNo(), app.getAppNo(), request.getAllotmentCode());
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Integer getNoOfVacateRequests() {
		try {
			return vacateRequestRepository.countByStatusEquals("P");
		}catch(Exception ex) {
			throw ex;
		}
	}
	
}
