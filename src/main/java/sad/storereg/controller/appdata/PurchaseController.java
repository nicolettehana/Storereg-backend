package sad.storereg.controller.appdata;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.PurchaseCreateDTO;
import sad.storereg.dto.appdata.PurchaseResponseDTO;
import sad.storereg.services.appdata.PurchaseService;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {
	
	private final PurchaseService purchaseService;
	
	 @GetMapping({ "", "/{category}" })
	    public Page<PurchaseResponseDTO> getPaginatedFirms(
	    		 @PathVariable(required = false) String category,
	    	        @RequestParam(defaultValue = "0") int page,
	    	        @RequestParam(defaultValue = "10") int size,
	    	        @RequestParam(defaultValue = "") String search,
	    	        @RequestParam(defaultValue = "") LocalDate startDate,
	    	        @RequestParam(defaultValue = "") LocalDate endDate
	    ) {
	        Pageable pageable = PageRequest.of(page, size);
	        return purchaseService.searchPurchases(startDate, endDate, category, search, pageable);
	    }
	 
	 @PostMapping("/create")
	    public ResponseEntity<String> savePurchase(@RequestBody PurchaseCreateDTO purchaseDTO) {

	        // Debug print - remove in production
	        System.out.println("Received Purchase:");
	        System.out.println(purchaseDTO);

	        // You can process & save this data however you need.

	        return ResponseEntity.ok(purchaseService.savePurchase(purchaseDTO));
	    }
}
