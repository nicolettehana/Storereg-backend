package sad.storereg.services.appdata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Firm;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.appdata.PurchaseRepository;
import sad.storereg.repo.master.CategoryRepository;
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
	
	private final ExcelServices excelService;
	
	private final CategoryRepository categoryRepository;
	
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
	    dto.setFileNo(p.getFileNo());
	    dto.setBillNo(p.getBillNo());
	    dto.setBillDate(p.getBillDate());
	    dto.setGstPercentage(p.getGstPercentage());
	    dto.setGst(p.getGstPercentage()!=null? (p.getGstPercentage()*p.getTotalCost())/100 : null);

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
	        System.out.println("Purchase category: "+category);		
	        itemDTO.setCategory(category);
	        itemDTO.setCategoryCode( entry.getValue().get(0).getItem().getCategory().getCode());

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
	    Double totalAmount = p.getItems()
	            .stream()
	            .mapToDouble(PurchaseItems::getAmount)
	            .sum();

	    dto.setItems(itemDTOs);
	    dto.setTotalCost(totalAmount);
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
        purchase.setFileNo(dto.getFileNo());
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
	
	public int getAvailableBalance(Long itemId, Long subItemId, Integer unitId, LocalDate date) {
		Integer availableStock = purchaseRepository.getAvailableStock(itemId, subItemId, unitId, date);
		//System.out.println("ItemID: "+itemId+" subItemId: "+subItemId+" unitId: "+unitId+" date: "+date+" available stock: "+availableStock);
	    return availableStock;
	}
	
	public String getAvailableBalanceAllUnits(Long itemId, Long subItemId, LocalDate date) {
		List<Object[]> availableStock = purchaseRepository.getAvailableStockForAllUnits(itemId, subItemId, date);
		
		String totalStock = availableStock.stream()
		        .map(r -> ((Number) r[2]).intValue() + " " + r[1])   // balance + unitName
		        .collect(Collectors.joining(", "));

		//System.out.println("ItemID: "+itemId+" subItemId: "+subItemId+" unitId: "+unitId+" date: "+date+" available stock: "+availableStock);
	    return totalStock;
	}
	
	public Map<String, Object> getFinancialYearReport(int year) {

	    LocalDate fromDate = LocalDate.of(year, 4, 1);
	    LocalDate toDate   = LocalDate.of(year + 1, 3, 31);

	    List<Object[]> results = purchaseRepository.getCategoryTotals(fromDate, toDate);

	    double total = 0;
	    List<Map<String, Object>> categories = new ArrayList<>();

	    for (Object[] row : results) {
	        String category      = (String) row[0];
	        String categoryCode  = (String) row[1];
	        Double amount        = (Double) row[2];

	        total += amount;

	        Map<String, Object> map = new HashMap<>();
	        map.put("category", category);
	        map.put("categoryCode", categoryCode);
	        map.put("value", amount);

	        categories.add(map);
	    }

	    Map<String, Object> response = new HashMap<>();
	    response.put("total", total);
	    response.put("categories", categories);

	    return response;
	}
	
	public boolean purchaseExist(Firm firm, YearRange yearRange, Category category) {
		LocalDate startDate = LocalDate.of(yearRange.getStartYear(), 1, 1);
		LocalDate endDate   = LocalDate.of(yearRange.getEndYear(), 12, 31);

		List<Purchase> purchases =
		        purchaseRepository.findPurchasesByFirmDateRangeAndCategory(
		                firm.getId(),
		                startDate,
		                endDate,
		                category.getCode()
		        );
		return (purchases.size()>0?true:false);
	}
	
	public byte[] exportPurchase(LocalDate startDate, LocalDate endDate, String categoryCode) {
		
		List<Purchase> purchases = purchaseRepository.getPurchases(
                startDate, endDate, categoryCode
        );
		
        List<PurchaseResponseDTO> dtoList = purchases.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

               Sheet sheet = workbook.createSheet("Purchases");
               Map<String, CellStyle> styles = excelService.createStyles(workbook);

               int rowIdx = 0;

               // =====================
               // TITLE
               // =====================
               rowIdx = excelService.createTitleRow(
                       workbook,
                       sheet,
                       rowIdx,
                       "Purchases",
                       0,
                       10
               );

               rowIdx++;

               // =====================
               // METADATA
               // =====================
               rowIdx = excelService.createLabelValueRow(
                       sheet,
                       rowIdx,
                       "Date:",
                       startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                               + " to "
                               + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                       styles.get("bold")
               );

               String categoryName = (categoryCode != null && !categoryCode.isEmpty())
                       ? categoryRepository.findByCode(categoryCode).get().getName()
                       : "All";

               rowIdx = excelService.createLabelValueRow(
                       sheet,
                       rowIdx,
                       "Category:",
                       categoryName,
                       styles.get("bold")
               );

               rowIdx++;

               // =====================
               // TABLE HEADER
               // =====================
               String[] headers = {
                       "Sl No.",
                       "Date of Purchase",
                       "Firm",
                       "Category",
                       "Particulars",
                       "",
                       "Quantity",
                       "Rate (₹)",
                       "Amount (₹)",
                       "Total",
                       "Remarks"
               };

               rowIdx = excelService.createTableHeaderRow(
                       sheet,
                       rowIdx,
                       headers,
                       styles.get("headerBorder")
               );
               
               int headerRowIndex = rowIdx - 1;
               
            // merge columns 2 and 3
      	     sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 4, 5));

      	     // ensure border style applies to merged cells
      	     Row headerRow1 = sheet.getRow(headerRowIndex);
      	     for (int col = 4; col <= 5; col++) {
      	         headerRow1.getCell(col).setCellStyle(styles.get("headerBorder"));
      	     }
               // =====================
               // TABLE DATA
               // =====================
               int slNo = 1;

               for (PurchaseResponseDTO purchase : dtoList) {

                   int purchaseStartRow = rowIdx;

                   for (ItemPurchaseDTO item : purchase.getItems()) {

                       // =====================
                       // ITEM WITHOUT SUB-ITEMS
                       // =====================
                       if (item.getSubItems() == null || item.getSubItems().isEmpty()) {
                    	   
                    	   int itemStartRow = rowIdx;

                           Row row = sheet.createRow(rowIdx++);

                           row.createCell(0).setCellValue(slNo);
                           row.createCell(1).setCellValue(purchase.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                           row.createCell(2).setCellValue(purchase.getFirmName());
                           row.createCell(3).setCellValue(item.getCategory());
                           row.createCell(4).setCellValue(item.getItemName());
                           row.createCell(6).setCellValue(item.getQuantity());
                           row.createCell(7).setCellValue(item.getRate()+" "+item.getUnit());
                           row.createCell(8).setCellValue(item.getAmount());
                           row.createCell(9).setCellValue(purchase.getTotalCost());
                           row.createCell(10).setCellValue(purchase.getRemarks());

                        // styles + wrap
                           for (int col = 0; col <= 10; col++) {
                               Cell cell = row.getCell(col);
                               if (cell == null) cell = row.createCell(col);

                               cell.setCellStyle(
                            		   (col == 2 || col == 4 || col == 9 || col == 10)
                                               ? styles.get("wrapBorder")
                                               : styles.get("border")
                               );
                           }

                           row.setHeight((short) -1);
                           
                           int itemEndRow = rowIdx - 1;

          	             // merge Item + SubItem horizontally
          	             sheet.addMergedRegion(
          	                     new CellRangeAddress(itemStartRow, itemEndRow, 4, 5)
          	             );
          	             excelService.applyBorder(
          	                     new CellRangeAddress(itemStartRow, itemEndRow, 4, 5),
          	                     sheet
          	             );
                       }

                       // =====================
                       // ITEM WITH SUB-ITEMS
                       // =====================
                       else {
                    	   int itemStartRow = rowIdx;
                    	   
                           for (SubItemPurchaseDTO sub : item.getSubItems()) {

                               Row row = sheet.createRow(rowIdx++);

                               row.createCell(0).setCellValue(slNo);
                               row.createCell(1).setCellValue(purchase.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                               row.createCell(2).setCellValue(purchase.getFirmName());
                               row.createCell(3).setCellValue(item.getCategory());
                               row.createCell(4).setCellValue(
                                       item.getItemName()
                               );
                               row.createCell(5).setCellValue(sub.getSubItemName());
                               row.createCell(6).setCellValue(sub.getQuantity());
                               row.createCell(7).setCellValue(sub.getRate()+" "+sub.getUnit());
                               row.createCell(8).setCellValue(sub.getAmount());
                               row.createCell(9).setCellValue(purchase.getTotalCost());
                               row.createCell(10).setCellValue(purchase.getRemarks());

                               for (int col = 0; col <= 10; col++) {
                                   Cell cell = row.getCell(col);
                                   if (cell == null) cell = row.createCell(col);

                                   cell.setCellStyle(
                                           (col == 2 || col == 4 || col == 5 || col == 9 || col == 10)
                                                   ? styles.get("wrapBorder")
                                                   : styles.get("border")
                                   );
                               }

                               row.setHeight((short) -1);
                           }
                           int itemEndRow = rowIdx - 1;

                           // merge Item name vertically
                           excelService.mergeVertically(sheet, itemStartRow, itemEndRow, 4);
                           excelService.mergeVertically(sheet, itemStartRow, itemEndRow, 3);
                       }
                   }

                   int purchaseEndRow = rowIdx - 1;

                   // =====================
                   // MERGES PER PURCHASE
                   // =====================
                   excelService.mergeVertically(sheet, purchaseStartRow, purchaseEndRow, 0); // Sl No
                   excelService.mergeVertically(sheet, purchaseStartRow, purchaseEndRow, 1); // Date
                   excelService.mergeVertically(sheet, purchaseStartRow, purchaseEndRow, 2); // Firm
                   excelService.mergeVertically(sheet, purchaseStartRow, purchaseEndRow, 9); // Total
                   excelService.mergeVertically(sheet, purchaseStartRow, purchaseEndRow, 10); // Remarks

                   slNo++;
               }

               // =====================
               // AUTO SIZE
               // =====================
               for (int i = 0; i < headers.length; i++) {
                   sheet.autoSizeColumn(i);
               }

               workbook.write(out);
               return out.toByteArray();

           } catch (IOException e) {
               throw new RuntimeException("Failed to export Purchases Excel", e);
           }
		
	}


}
