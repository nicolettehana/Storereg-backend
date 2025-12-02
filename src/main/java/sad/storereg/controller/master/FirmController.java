package sad.storereg.controller.master;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.CreateFirmDTO;
import sad.storereg.dto.master.FirmYearDTO;
import sad.storereg.dto.master.FirmsDTO;
import sad.storereg.models.master.Firm;
import sad.storereg.services.master.FirmService;

@RestController
@RequestMapping("/firms")
@RequiredArgsConstructor
public class FirmController {
	
	private final FirmService firmService;

    @GetMapping({ "", "/{category}" })
    public Page<FirmsDTO> getPaginatedFirms(
    		 @PathVariable(required = false) String category,
    	        @RequestParam(defaultValue = "0") int page,
    	        @RequestParam(defaultValue = "10") int size,
    	        @RequestParam(defaultValue = "") String search,
    	        @RequestParam(defaultValue = "") Integer yearRangeId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        if(yearRangeId!=null)
        	return firmService.getFirms(yearRangeId, category, pageable);

        return firmService.getFirms(pageable, search, category);
    }
    
    @PostMapping
    public ResponseEntity<?> createFirm(@RequestBody CreateFirmDTO request) {
        
        return ResponseEntity.ok(firmService.createFirm(request));
    }
    
    @GetMapping({ "/list" })
    public List<FirmsDTO> getListFirms(
    ) {

        return firmService.getFirmsList();
    }
    
    @PostMapping("/add-approved")
    public ResponseEntity<?> createFirmYear(@RequestBody FirmYearDTO request) {
    	return null;
        //return ResponseEntity.ok(firmService.createFirmYear(request));
    }

}
