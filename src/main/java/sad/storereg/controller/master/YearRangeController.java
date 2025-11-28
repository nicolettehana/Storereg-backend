package sad.storereg.controller.master;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.YearRange;
import sad.storereg.services.master.YearRangeService;

@RestController
@RequestMapping("/year-range")
@RequiredArgsConstructor
public class YearRangeController {
	
	private final YearRangeService yearRangeService;

    @GetMapping
    public List<YearRange> getAllYearRanges() {
        return yearRangeService.getAllYearRanges();
    }

}
