package sad.storereg.controller.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.SubItems;
import sad.storereg.models.master.Item;
import sad.storereg.services.master.RateService;
import sad.storereg.dto.master.ItemMixin;
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
		
		//ObjectMapper mapper = new ObjectMapper();
	    //mapper.addMixIn(Item.class, ItemMixin.class);

		return rateService.getRates(category, yearRange, PageRequest.of(page, size));
		
		//return rates.map(rate ->
        //mapper.convertValue(rate, Rate.class)
//		
//		return rates.map(rate -> {
//
//	        // --- CASE 1: object_type = item ---
//	        if ("item".equalsIgnoreCase(rate.getObjectType()) && rate.getItem() != null) {
//	            Item item = rate.getItem();
//	            item.setRate(rate.getRate());                     // attach rate
//	            item.setUnit(rate.getUnit().getUnit());       // attach unit
//	        }
//
//	        // --- CASE 2: object_type = subItem ---
//	        if ("subItem".equalsIgnoreCase(rate.getObjectType()) && rate.getSubItem() != null) {
//	            SubItems s = rate.getSubItem();
//	            s.setRate(rate.getRate());                        // attach rate
//	            s.setUnit(rate.getUnit().getUnit());          // attach unit
//	        }
//
//	        return rate;
//	    });

	}
}
