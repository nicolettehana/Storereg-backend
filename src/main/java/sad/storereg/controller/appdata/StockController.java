package sad.storereg.controller.appdata;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.CategoryStockResponse;
import sad.storereg.services.appdata.StockBalanceService;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {
	
	 private final StockBalanceService stockService;

	    @GetMapping("/{level}")
	    public ResponseEntity<List<CategoryStockResponse>> getStock(
	    		@PathVariable Integer level
	    ) {
	        List<CategoryStockResponse> response = stockService.getStockFiltered(level);
	        return ResponseEntity.ok(response);
	    }

}
