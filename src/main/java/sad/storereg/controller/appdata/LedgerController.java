package sad.storereg.controller.appdata;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.LedgerResponse;
import sad.storereg.services.appdata.LedgerService;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {
	
	private final LedgerService ledgerService;

	@GetMapping
    public ResponseEntity<Page<LedgerResponse>> getLedger(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort, // optional
            @RequestParam(defaultValue = "") String categoryCode
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<LedgerResponse> ledger = ledgerService.getLedger(startDate, endDate, 1, categoryCode, pageable);
        return ResponseEntity.ok(ledger);
    }

}
