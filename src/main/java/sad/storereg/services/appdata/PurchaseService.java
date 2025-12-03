package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ItemPurchaseDTO;
import sad.storereg.dto.appdata.PurchaseResponseDTO;
import sad.storereg.dto.appdata.SubItemPurchaseDTO;
import sad.storereg.models.appdata.Purchase;
import sad.storereg.models.appdata.PurchaseItems;
import sad.storereg.repo.appdata.PurchaseRepository;

@RequiredArgsConstructor
@Service
public class PurchaseService {
	
	private final PurchaseRepository purchaseRepository;
	
	public Page<PurchaseResponseDTO> searchPurchases(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String searchValue,
            Pageable pageable) {

        Page<Purchase> page = purchaseRepository.searchPurchases(
                startDate, endDate, category, searchValue, pageable
        );

        return page.map(this::convertToDTO);
    }

	private PurchaseResponseDTO convertToDTO(Purchase p) {

	    PurchaseResponseDTO dto = new PurchaseResponseDTO();

	    dto.setPurchaseId(p.getId());
	    dto.setFirmName(p.getFirm().getFirm());
	    dto.setRemarks(p.getRemarks());
	    dto.setTotalCost(p.getTotalCost());
	    dto.setDate(p.getDate());

	    // Group items by item name
	    Map<String, List<PurchaseItems>> itemGroup = p.getItems()
	            .stream()
	            .collect(Collectors.groupingBy(pi -> pi.getItem().getName()));

	    List<ItemPurchaseDTO> itemDTOs = new ArrayList<>();

	    for (var entry : itemGroup.entrySet()) {

	        ItemPurchaseDTO itemDTO = new ItemPurchaseDTO();
	        itemDTO.setItemName(entry.getKey());

	        // Set category (all grouped items have the same category)
	        String category = entry.getValue().get(0).getItem().getCategory().getName();
	        itemDTO.setCategory(category);

	        List<SubItemPurchaseDTO> subItems = entry.getValue()
	                .stream()
	                .map(pi -> {
	                    if (pi.getSubItem() == null) {
	                        itemDTO.setQuantity(pi.getQuantity());
	                        itemDTO.setRate(pi.getRate());
	                        itemDTO.setAmount(pi.getAmount());
	                        itemDTO.setUnit(pi.getUnit().getUnit());
	                        return null;
	                    }

	                    SubItemPurchaseDTO sd = new SubItemPurchaseDTO();
	                    sd.setSubItemName(pi.getSubItem().getName());
	                    sd.setQuantity(pi.getQuantity());
	                    sd.setRate(pi.getRate());
	                    sd.setAmount(pi.getAmount());
	                    sd.setUnit(pi.getUnit().getUnit());
	                    return sd;
	                })
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());

	        itemDTO.setSubItems(subItems);
	        itemDTOs.add(itemDTO);
	    }

	    dto.setItems(itemDTOs);
	    return dto;
	}

}
