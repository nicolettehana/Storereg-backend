package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import sad.storereg.models.master.Item;
import sad.storereg.models.master.SubItems;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

@Service
public class ExcelServices {
	
	public Map<String, CellStyle> createStyles(Workbook workbook) {

        Map<String, CellStyle> styles = new HashMap<>();

        // ===== TITLE STYLE =====
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        styles.put("title", titleStyle);

        // ===== BOLD STYLE =====
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        styles.put("bold", boldStyle);

        // ===== TABLE HEADER STYLE =====
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        styles.put("header", headerStyle);
        
        // ===== TABLE BORDER STYLE =====
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        styles.put("border", borderStyle);

        // ===== HEADER + BORDER STYLE =====
        CellStyle headerBorderStyle = workbook.createCellStyle();
        headerBorderStyle.cloneStyleFrom(styles.get("header"));
        headerBorderStyle.setBorderTop(BorderStyle.THIN);
        headerBorderStyle.setBorderBottom(BorderStyle.THIN);
        headerBorderStyle.setBorderLeft(BorderStyle.THIN);
        headerBorderStyle.setBorderRight(BorderStyle.THIN);
        styles.put("headerBorder", headerBorderStyle);
        
        // ===== WRAP + BORDER STYLE =====
        CellStyle wrapBorderStyle = workbook.createCellStyle();
        wrapBorderStyle.setWrapText(true);
        wrapBorderStyle.setBorderTop(BorderStyle.THIN);
        wrapBorderStyle.setBorderBottom(BorderStyle.THIN);
        wrapBorderStyle.setBorderLeft(BorderStyle.THIN);
        wrapBorderStyle.setBorderRight(BorderStyle.THIN);
        wrapBorderStyle.setVerticalAlignment(VerticalAlignment.TOP);

        styles.put("wrapBorder", wrapBorderStyle);



        return styles;
    }
	
	public void createCell(Row row, int col, Object value, Map<String, CellStyle> styles) {
	    Cell cell = row.createCell(col);

	    if (value instanceof Number) {
	        cell.setCellValue(((Number) value).doubleValue());
	    } else {
	        cell.setCellValue(value != null ? value.toString() : "");
	    }

	    cell.setCellStyle(styles.get("wrapBorder"));
	}
	
	public void applyBorder(CellRangeAddress region, Sheet sheet) {
	    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}
	
	public void createExcelContentItems(
	        Sheet sheet,
	        List<Item> items,
	        String category,
	        String categoryName,
	        Map<String, CellStyle> styles
	) {

	    int rowNum = 0;

	    // ===== TITLE =====
	    Row titleRow = sheet.createRow(rowNum++);
	    Cell titleCell = titleRow.createCell(0);
	    titleCell.setCellValue("Items");
	    titleCell.setCellStyle(styles.get("title"));
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

	    rowNum++;

	    // ===== CATEGORY =====
	    Row categoryRow = sheet.createRow(rowNum++);
	    categoryRow.createCell(0).setCellValue("Category:");
	    categoryRow.createCell(1).setCellValue(categoryName);
	    //categoryRow.createCell(1).setCellValue(
	    //        category == null || category.isBlank() ? "All" : category);

	    // ===== DATE =====
	    Row dateRow = sheet.createRow(rowNum++);
	    dateRow.createCell(0).setCellValue("Date:");
	    dateRow.createCell(1).setCellValue(LocalDate.now().toString());

	    rowNum++;

	    // ===== TABLE HEADER =====
	    Row headerRow = sheet.createRow(rowNum++);

	 // Sl No
	 Cell slCell = headerRow.createCell(0);
	 slCell.setCellValue("Sl. No.");
	 slCell.setCellStyle(styles.get("headerBorder"));

	 // Item (merged header)
	 Cell itemHeaderCell = headerRow.createCell(1);
	 itemHeaderCell.setCellValue("Item");
	 itemHeaderCell.setCellStyle(styles.get("headerBorder"));

	 // Create empty Sub Item header cell (required before merge)
	 Cell subItemHeaderCell = headerRow.createCell(2);
	 subItemHeaderCell.setCellStyle(styles.get("headerBorder"));

	 // Category
	 Cell categoryCell = headerRow.createCell(3);
	 categoryCell.setCellValue("Category");
	 categoryCell.setCellStyle(styles.get("headerBorder"));

	 // Balance
	 Cell balanceCell = headerRow.createCell(4);
	 balanceCell.setCellValue("Balance");
	 balanceCell.setCellStyle(styles.get("headerBorder"));

	 // ===== MERGE Item + Sub Item HEADER =====
	 CellRangeAddress itemHeaderMerge =
	         new CellRangeAddress(headerRow.getRowNum(),
	                              headerRow.getRowNum(),
	                              1, 2);

	 sheet.addMergedRegion(itemHeaderMerge);

	 RegionUtil.setBorderTop(BorderStyle.THIN, itemHeaderMerge, sheet);
	 RegionUtil.setBorderBottom(BorderStyle.THIN, itemHeaderMerge, sheet);
	 RegionUtil.setBorderLeft(BorderStyle.THIN, itemHeaderMerge, sheet);
	 RegionUtil.setBorderRight(BorderStyle.THIN, itemHeaderMerge, sheet);

	    int slNo = 1;

	    for (Item item : items) {

	        int startRow = rowNum;
	        int rowsCreated = 0;

	        boolean hasSubItems =
	                item.getSubItems() != null && !item.getSubItems().isEmpty();

	        if (hasSubItems) {

	            // ===== One row per sub-item =====
	            for (SubItems subItem : item.getSubItems()) {
	                Row row = sheet.createRow(rowNum++);

	                // Sub Item
	                createCell(
	                        row, 2, subItem.getName(), styles);

	                // Balance
	                createCell(
	                        row, 4, safeBalance(subItem.getBalance()), styles);

	                rowsCreated++;
	            }

	        } else {

	            // ===== Single row =====
	            Row row = sheet.createRow(rowNum++);

	            // Balance
	            createCell(
	                    row, 4, safeBalance(item.getBalance()), styles);

	            rowsCreated = 1;
	        }

	        int endRow = startRow + rowsCreated - 1;
	        Row firstRow = sheet.getRow(startRow);

	        // ===== Sl No =====
	        createCell(firstRow, 0, slNo, styles);

	        // ===== Category =====
	        createCell(
	                firstRow, 3, item.getCategory().getName(), styles);

	        if (hasSubItems) {

	            // ===== Item only in Item column =====
	            createCell(
	                    firstRow, 1, item.getName(), styles);

	            // ===== Vertical merge =====
	            merge(sheet, startRow, endRow, 0); // Sl No
	            merge(sheet, startRow, endRow, 1); // Item
	            merge(sheet, startRow, endRow, 3); // Category

	        } else {

	            // ===== Merge Item + Sub Item horizontally =====
	            createCell(
	                    firstRow, 1, item.getName(), styles);

	            mergeHorizontal(sheet, startRow, 1, 2);
	        }

	        slNo++;
	    }



	    // ===== AUTO SIZE =====
	    for (int i = 0; i < 4; i++) {
	        sheet.autoSizeColumn(i);
	    }
	}

	private void merge(Sheet sheet, int startRow, int endRow, int col) {
		if (startRow >= endRow) {
	        return;
	    }
	    CellRangeAddress region =
	            new CellRangeAddress(startRow, endRow, col, col);
	    sheet.addMergedRegion(region);

	    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}
	
	private void mergeHorizontal(Sheet sheet, int row, int colStart, int colEnd) {

	    CellRangeAddress region =
	            new CellRangeAddress(row, row, colStart, colEnd);

	    sheet.addMergedRegion(region);

	    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
	    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}

	private String safeBalance(Object balance) {
	    if (balance == null) {
	        return "0";
	    }
	    if (balance instanceof Number) {
	        return String.valueOf(balance);
	    }
	    String val = balance.toString().trim();
	    return val.isEmpty() ? "0" : val;
	}



}
