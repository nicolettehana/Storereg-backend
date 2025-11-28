package sad.storereg.controller.appdata;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.dto.appdata.VacateRequestDTO;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Vacated;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.AllotmentServices;
import sad.storereg.services.appdata.VacateRequestServices;
import sad.storereg.services.appdata.VacatedServices;

@RestController
@RequestMapping("/vacate")
@RequiredArgsConstructor
public class VacatedController {
	
	private final VacatedServices vacatedService;
	private final AllotmentServices allotmentService;
	private final VacateRequestServices vacateReqService;
	
	@Auditable
	@Transactional
	@PostMapping
	public ResponseEntity<Map<String,String>> markAsVacated(@Valid @RequestBody VacateRequestDTO request, @AuthenticationPrincipal User user) {
		
		try {			
			Map<String, String> responseMap = new HashMap<>();
			
			Vacated vacated = new Vacated();
			vacated.setAppNo(request.getAppNo());
			vacated.setQuarterNo(request.getQuarterNo());
			vacated.setVacateDate(request.getVacateDate());
			vacated.setEntrydate(new Date());
			
			Allotments allotment=allotmentService.vacateAllotment(vacated);
			
			request.setStatus("A");
			vacateReqService.createVacateRequest(request, user.getUsername());
			
			vacatedService.markAsVacated(vacated, user.getUsername(), allotment.getId());
			
			responseMap.put("detail","Successfully marked occupant as vacated");
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
			
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	@PostMapping(path="/getVacated", params = { "page", "size" })
	public Page<QuartersDTO> getVacated(@Valid @RequestBody MyFilter filter, @RequestParam("page") final int page,
            @RequestParam("size") final int size) throws Exception{
		try {
			
			if(filter.getFromDate()==null || filter.getToDate()==null)
				throw new BadRequestException("Date range is required");
			
			return vacatedService.getVacated(filter, page, size);			
			
		}catch(Exception ex) {
			throw ex;
		}
	}

}
