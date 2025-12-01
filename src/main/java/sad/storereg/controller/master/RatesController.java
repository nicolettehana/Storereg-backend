package sad.storereg.controller.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.SubItems;
import sad.storereg.models.master.Item;
import sad.storereg.services.master.RateService;
import sad.storereg.dto.master.ItemDTO;
import sad.storereg.dto.master.ItemMixin;
import sad.storereg.dto.master.ItemRateCreateDTO;
import sad.storereg.dto.master.ItemRateDTO;

import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class RatesController {

	private final RateService rateService;
	
	@GetMapping({ "", "/{category}" })
	public Page<ItemRateDTO> filterRates(@PathVariable(required = false) String category,
	@RequestParam(required = false) Integer yearRange,
	@RequestParam int page,
	@RequestParam int size,
	@RequestParam(defaultValue = "") String search) {

		return rateService.getRates(category, yearRange, PageRequest.of(page, size));
		

	}
	
	@PostMapping
    public ResponseEntity<?> createRate(@RequestBody ItemRateCreateDTO request) {
		System.out.println("Hey: "+request);
        return ResponseEntity.ok(rateService.createRate(request));
		//return ResponseEntity.ok("ok");
    }
}
