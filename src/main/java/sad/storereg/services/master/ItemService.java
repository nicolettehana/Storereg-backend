package sad.storereg.services.master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.CategoryCountDTO;
import sad.storereg.dto.master.ItemDTO;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.SubItems;
import sad.storereg.models.master.Unit;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.ItemRepository;
import sad.storereg.services.appdata.ExcelServices;
import sad.storereg.services.appdata.PurchaseService;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;
	private final PurchaseService purchaseService;
	private final ExcelServices excelService;
	private final MasterDataServices masterDataServices;

    public Page<Item> getItems(Pageable pageable, String search, Integer officeCode, String category) {
    	
    	Page<Item> page;
    	
    	if(search!=null && search.length()>0) {
    		page = itemRepository.searchByItemOrSubItemNameAndOfficeCode(search, officeCode, pageable);
    	}
    	else if(category==null || category.equals("") || category.equals("All"))
    		page = itemRepository.findAllByOfficeCode(officeCode, pageable);
    	else
    		page = itemRepository.findAllByCategory_CodeAndOfficeCode(category, officeCode, pageable);
    	
    	//page.forEach(item -> item.setBalance(purchaseService.getAvailableBalance(item.getId(),item.getSubItems()==null?null:item.getSubItems().getId(), rate.getUnit().getId(), issueDate==null?LocalDate.now():null)));

    	page.forEach(item -> {
    		if (item.getSubItems() != null) {
                item.getSubItems().forEach(subItem -> {
                    subItem.setBalance(purchaseService.getAvailableBalanceAllUnits(item.getId(),item.getSubItems().size()==0?null:subItem.getId(),LocalDate.now())); 
                });
            }
            if(item.getSubItems().size()==0) {
            	item.setBalance(purchaseService.getAvailableBalanceAllUnits(item.getId(),null,LocalDate.now()));
            };
        });
    	
        return page;
    }
    
    public List<Item> getItemsList(String search, String category, Integer officeCode) {
    	if(category==null || category.equals("") || category.equals("All"))
    		return itemRepository.findAllByOfficeCode(officeCode);
    	else return itemRepository.findAllByCategory_CodeAndOfficeCode(category, officeCode);
    }

    public String createItem(ItemDTO request, Integer officeCode) {

        Item item = new Item();
        item.setName(request.getItemName());
        item.setOfficeCode(officeCode);
        
        Category category = categoryRepository.findByCodeAndOfficeCode(request.getCategory(),officeCode)
                .orElseThrow(() -> new RuntimeException("Category code not found: " + request.getCategory()));
        item.setCategory(category);

     // Handle sub-items only if required
        if (Boolean.TRUE.equals(request.getHasSubItems()) &&
            request.getSubItems() != null && !request.getSubItems().isEmpty()) {

        	// Map SubItemDTO -> SubItems entity
            List<SubItems> subItemList = request.getSubItems().stream()
                .map(dto -> {
                    SubItems s = new SubItems();
                    s.setName(dto.getName());          
                    Unit unit = masterDataServices.getUnit(dto.getUnitId());
                    s.setBaseUnit(unit);
                    s.setItem(item);                    
                    return s;
                })
                .collect(Collectors.toList());

            item.setSubItems(subItemList);

        } else {
            item.setSubItems(null);
            Unit unit = masterDataServices.getUnit(request.getUnitId());
            item.setBaseUnit(unit);
        }

        itemRepository.save(item);
        
        return("Item added");
    }
    
    public List<CategoryCountDTO> getCategoryCounts(Integer officeCode) {
        return itemRepository.getCategoryCounts(officeCode);
    }
    
    public Long getTotalItems(Integer officeCode) {
    	return itemRepository.getAbsoluteTotal(officeCode);
    }
    
    public byte[] getItems(String category, Integer officeCode) throws IOException {

        List<Item> items = getItemsList(null,category, officeCode);

     // Calculate balances
        items.forEach(item -> {
            if (item.getSubItems() != null && !item.getSubItems().isEmpty()) {
                item.getSubItems().forEach(subItem -> {
                    subItem.setBalance(
                        purchaseService.getAvailableBalanceAllUnits(
                            item.getId(), subItem.getId(), LocalDate.now()));
                });
            } else {
                item.setBalance(
                    purchaseService.getAvailableBalanceAllUnits(
                        item.getId(), null, LocalDate.now()));
            }
        });

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Items");
            Map<String, CellStyle> styles = excelService.createStyles(workbook);

            String categoryName = (category!=null && category.length()>0)?categoryRepository.findByCodeAndOfficeCode(category, officeCode).get().getName():"All";
            excelService.createExcelContentItems(sheet, items, category, categoryName, styles, workbook);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
