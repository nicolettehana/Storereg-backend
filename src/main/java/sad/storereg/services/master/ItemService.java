package sad.storereg.services.master;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.CategoryCountDTO;
import sad.storereg.dto.master.ItemDTO;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.SubItems;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;

    public Page<Item> getItems(Pageable pageable, String search, String category) {
    	if(category==null || category.equals("") || category.equals("All"))
    		return itemRepository.findAll(pageable);
    	else return itemRepository.findAllByCategory_Code(category, pageable);
    }
    
    public List<Item> getItemsList(String search, String category) {
    	if(category==null || category.equals("") || category.equals("All"))
    		return itemRepository.findAll();
    	else return itemRepository.findAllByCategory_Code(category);
    }

    public String createItem(ItemDTO request) {

        Item item = new Item();
        item.setName(request.getItemName());
        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new RuntimeException("Category code not found: " + request.getCategory()));
        item.setCategory(category);

     // Handle sub-items only if required
        if (Boolean.TRUE.equals(request.getHasSubItems()) &&
            request.getSubItems() != null && !request.getSubItems().isEmpty()) {

            List<SubItems> subItemList = request.getSubItems().stream()
                .map(name -> {
                    SubItems s = new SubItems();
                    s.setName(name);
                    s.setItem(item); // link back
                    return s;
                })
                .collect(Collectors.toList());

            item.setSubItems(subItemList);

        } else {
            item.setSubItems(null);
        }

        itemRepository.save(item);
        
        return("Item added");
    }
    
    public List<CategoryCountDTO> getCategoryCounts() {
        return itemRepository.getCategoryCounts();
    }
    
    public Long getTotalItems() {
    	return itemRepository.getAbsoluteTotal();
    }
}
