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
import sad.storereg.models.appdata.Issue;
import sad.storereg.models.appdata.IssueItem;
import sad.storereg.repo.appdata.IssueRepository;
import sad.storereg.repo.master.ItemRepository;
import sad.storereg.repo.master.SubItemRepository;
import sad.storereg.repo.master.UnitRepository;

@Service
@RequiredArgsConstructor
public class IssueService {
	
	private final IssueRepository issueRepository;
	private final ItemRepository itemRepository;
	private final SubItemRepository subItemRepository;
	private final UnitRepository unitRepository;
	
	public Page<PurchaseResponseDTO> searchIssues(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String searchValue,
            Pageable pageable) {

		
      Page<Issue> page = issueRepository.searchIssues(
      startDate, endDate, category, searchValue, pageable);
      
      page.forEach(issue ->
      issue.getItems().removeIf(item ->
              category != null && !category.equals(item.getCategoryCode())
      )
      );

        return page.map(this::convertToDTO);
    }

	private PurchaseResponseDTO convertToDTO(Issue p) {

	    PurchaseResponseDTO dto = new PurchaseResponseDTO();

	    dto.setPurchaseId(p.getId());
	    dto.setIssuedTo(p.getIssueTo());
	    dto.setRemarks(p.getRemarks());
	    dto.setDate(p.getDate());
	    
	    Map<String, List<IssueItem>> itemGroup = p.getItems()
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
	                        itemDTO.setUnit(pi.getUnit().getUnit());
	                        return null;
	                    }

	                    SubItemPurchaseDTO sd = new SubItemPurchaseDTO();
	                    sd.setSubItemName(pi.getSubItem().getName());
	                    sd.setQuantity(pi.getQuantity());
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
	
	public String saveIssue(PurchaseCreateDTO dto) {
		System.out.println("DTO: "+dto
				);        
		Issue issue = new Issue();
        issue.setDate(dto.getIssueDate());
        issue.setEntrydate(LocalDateTime.now());
        issue.setRemarks(dto.getRemarks());
        issue.setIssueTo(dto.getIssueTo());
     
        // Convert items
        List<IssueItem> items = dto.getItems().stream().map(itemDTO -> {

            IssueItem item = new IssueItem();
            item.setIssue(issue);

            // Item
            item.setItem(itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found")));

            if (itemDTO.getSubItemId() != null) {
                item.setSubItem(subItemRepository.findById(itemDTO.getSubItemId())
                        .orElseThrow(() -> new RuntimeException("SubItem not found")));
            }

            // Unit
            item.setUnit(unitRepository.findById(itemDTO.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found")));

            item.setQuantity(itemDTO.getQuantity());
            item.setCategoryCode(itemDTO.getCategoryCode());
System.out.println("Item: "+item);
            return item;

        }).toList();


        issue.setItems(items);

        // 4. Save (cascade saves items)
         issueRepository.save(issue);
         return "Issue added";
    }


}
