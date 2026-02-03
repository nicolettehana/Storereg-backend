package sad.storereg.controller.master;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.YearRange;
import sad.storereg.services.master.YearRangeService;

@RestController
@RequestMapping("/year-range")
@RequiredArgsConstructor
public class YearRangeController {
	
	private final YearRangeService yearRangeService;

    @GetMapping
    public List<YearRange> getAllYearRanges(@AuthenticationPrincipal User user) {
        return yearRangeService.getAllYearRanges(user.getOfficeCode());
    }
    
    @Auditable
    @PostMapping
    public ResponseEntity<?> createYearRange(@RequestBody YearRange request, @AuthenticationPrincipal User user) {
    	try {
        return ResponseEntity.ok(yearRangeService.createYearRange(request, user.getOfficeCode()));
		//return ResponseEntity.ok("ok");
    	}catch(Exception ex) {
    		throw ex;
    	}
    }

}
