package sad.storereg.controller.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.master.ItemDTO;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Item;
import sad.storereg.services.master.ItemService;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

    @GetMapping({ "", "/{category}" })
    public Page<Item> getPaginatedItems(
    		 @PathVariable(required = false) String category,
    	        @RequestParam(defaultValue = "0") int page,
    	        @RequestParam(defaultValue = "10") int size,
    	        @RequestParam(defaultValue = "") String search,
    	        @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return itemService.getItems(pageable, search, user.getOfficeCode(), category);
    }
    
    @GetMapping({ "/list/{category}" })
    public List<Item> getListItems(
    		 @PathVariable(required = false) String category,
    	        @RequestParam(defaultValue = "") String search,
    	        @AuthenticationPrincipal User user
    ) {

        return itemService.getItemsList(search, category, user.getOfficeCode());
    }

    @Auditable
    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemDTO request, @AuthenticationPrincipal User user) {
      
        return ResponseEntity.ok(itemService.createItem(request, user.getOfficeCode()));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryCounts(@AuthenticationPrincipal User user) {
    	 Map<String, Object> response = new HashMap<>();
         response.put("total", itemService.getTotalItems(user.getOfficeCode()));
         response.put("byCategory", itemService.getCategoryCounts(user.getOfficeCode()));

         //return response;
        //List<CategoryCountDTO> counts = itemService.getCategoryCounts();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportItemsToExcel(
            @RequestParam(required = false) String category,
            HttpServletResponse response,
            @AuthenticationPrincipal User user
    ) throws IOException {

    	byte[] excelData = itemService.getItems(category, user.getOfficeCode());
    	
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=itemsS.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

}
