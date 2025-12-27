package sad.storereg.services.appdata;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

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


        return styles;
    }

}
