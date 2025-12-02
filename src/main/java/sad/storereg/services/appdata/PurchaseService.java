package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        dto.setCategory(
                p.getItems().isEmpty()
                        ? null
                        : p.getItems().get(0).getItem().getCategory().getCode()
        );

        Map<String, List<PurchaseItems>> itemGroup = p.getItems()
                .stream()
                .collect(Collectors.groupingBy(pi -> pi.getItem().getName()));

        List<ItemPurchaseDTO> itemDTOs = new ArrayList<>();

        for (var entry : itemGroup.entrySet()) {

            ItemPurchaseDTO itemDTO = new ItemPurchaseDTO();
            itemDTO.setItemName(entry.getKey());

            List<SubItemPurchaseDTO> subItems = entry.getValue().stream()
                    .map(pi -> {
                        SubItemPurchaseDTO sd = new SubItemPurchaseDTO();
                        sd.setSubItemName(pi.getSubItem() == null ? null : pi.getSubItem().getName());
                        sd.setQuantity(pi.getQuantity());
                        sd.setRate(pi.getRate());
                        sd.setAmount(pi.getAmount());
                        return sd;
                    })
                    .collect(Collectors.toList());

            itemDTO.setSubItems(subItems);
            itemDTOs.add(itemDTO);
        }

        dto.setItems(itemDTOs);
        return dto;
    }
}
