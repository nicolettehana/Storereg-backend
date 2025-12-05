package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import sad.storereg.dto.appdata.PurchaseCreateDTO;
import sad.storereg.dto.appdata.PurchaseResponseDTO;
import sad.storereg.dto.appdata.SubItemPurchaseDTO;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.appdata.Purchase;
import sad.storereg.models.appdata.PurchaseItems;
import sad.storereg.models.master.Firm;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.appdata.PurchaseRepository;
import sad.storereg.repo.master.FirmsRepository;
import sad.storereg.repo.master.ItemRepository;
import sad.storereg.repo.master.SubItemRepository;
import sad.storereg.repo.master.UnitRepository;
import sad.storereg.repo.master.YearRangeRepository;
import sad.storereg.services.master.RateService;

@RequiredArgsConstructor
@Service
public class PurchaseService {
	
	private final PurchaseRepository purchaseRepository;
	
	private final FirmsRepository firmRepository;
	
	private final UnitRepository unitRepository;
	
	private final ItemRepository itemRepository;
	
	private final SubItemRepository subItemRepository;
	
	private final RateService rateService;
	
	private final YearRangeRepository yearRangeRepository;
	
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
	
	public String savePurchase(PurchaseCreateDTO dto) {
        // 1. Fetch Firm
        Firm firm = firmRepository.findById(dto.getFirmId())
                .orElseThrow(() -> new RuntimeException("Firm not found"));

        // 2. Create Purchase entity
        Purchase purchase = new Purchase();
        purchase.setDate(dto.getPurchaseDate());   // Already LocalDate
        purchase.setFirm(firm);
        purchase.setEntryDate(LocalDateTime.now());
        purchase.setRemarks(dto.getRemarks());
        //purchase.setTotalCost(dto.getTotalCost());
        
        

     // Convert items
        List<PurchaseItems> items = dto.getItems().stream().map(itemDTO -> {

            PurchaseItems item = new PurchaseItems();
            item.setPurchase(purchase);

            // Item
            item.setItem(itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found")));

            // Sub Item (nullable)
            if (itemDTO.getSubItemId() != null) {
                item.setSubItem(subItemRepository.findById(itemDTO.getSubItemId())
                        .orElseThrow(() -> new RuntimeException("SubItem not found")));
            }

            // Unit
            item.setUnit(unitRepository.findById(itemDTO.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found")));

            int year = dto.getPurchaseDate().getYear();
            YearRange yearRange = yearRangeRepository
                    .findByStartYearLessThanEqualAndEndYearGreaterThanEqual(year, year)
                    .orElseThrow(() -> new ObjectNotFoundException("Year not found"));

            item.setQuantity(itemDTO.getQuantity());
            Double rate = rateService.getRate(
                    itemDTO.getUnitId(),
                    itemDTO.getItemId(),
                    itemDTO.getSubItemId(),
                    yearRange.getId()
            );

            item.setRate(rate);
            item.setAmount(rate * itemDTO.getQuantity());

            return item;

        }).toList();

        // Calculate total cost AFTER mapping
        Double totalCost = items.stream()
                .mapToDouble(PurchaseItems::getAmount)
                .sum();

        purchase.setItems(items);
        purchase.setTotalCost(totalCost);

        // 4. Save (cascade saves items)
         purchaseRepository.save(purchase);
         return "Purchase added";
    }

	}
