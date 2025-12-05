package sad.storereg.controller.appdata;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.PurchaseResponseDTO;
import sad.storereg.services.appdata.IssueService;

@RestController
@RequestMapping("/issue")
@RequiredArgsConstructor
public class IssueController {
	
	private final IssueService issueService;
	
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
        return issueService.searchIssues(startDate, endDate, category, search, pageable);
    }

}
