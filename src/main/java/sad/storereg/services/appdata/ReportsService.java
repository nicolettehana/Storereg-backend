package sad.storereg.services.appdata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import net.sf.jasperreports.engine.JREmptyDataSource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.FormPath;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.FormPathRepository;

@Service
@RequiredArgsConstructor
public class ReportsService {

	@Value("${reports.dir}")
	private String reportsPath;
	
	private final FormPathRepository formPathRepo;
	
	private final FormServices formServices;
	
	private final CoreServices coreService;
	
	@Value("${orders.dir}")
    private String ordersDir;
	
	@Transactional
	public void generateForm(HttpServletResponse response, Applications application) throws JRException, IOException {
		
		List<Map<String, Object>> data = getFormData(application);
		
		//File file = ResourceUtils.getFile("classpath:Application.jrxml");

		//JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
		//For Production
		InputStream jasperStream = this.getClass().getResourceAsStream("/Application.jasper");
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
		
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("reportParameter", "FormGADB");
		// Fill Jasper report
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		// Export report
		JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
	}
	
	List<Map<String, Object>> getFormData(Applications application) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("applicationNo", application.getAppNo());
		dataMap.put("fullName", application.getName());
		dataMap.put("basicPay", application.getBasicPay());
		dataMap.put("payScale", application.getScaleOfPay());
		dataMap.put("designation",application.getDesignation());
		dataMap.put("department", application.getDepartmentOrDirectorate());
		dataMap.put("officeAddress", application.getOfficeAddress()+" "+application.getOfficeTelephone());
		dataMap.put("dateEmployed", java.sql.Date.valueOf(application.getDateEmployed()));
		dataMap.put("dateRetirement", java.sql.Date.valueOf(application.getDateOfRetirement()));
		dataMap.put("gender", application.getGender());
		dataMap.put("maritalStatus", application.getMaritalStatus());
		dataMap.put("employmentStatus", application.getEmploymentStatus());
		dataMap.put("spouseAccommodation", application.getSpouseAccommodation());
		dataMap.put("accommodationDetails", application.getAccommdationDetails());
		dataMap.put("service", application.getService());
		dataMap.put("otherService", application.getOtherServicesDetails());
		dataMap.put("deputation", application.getCentralDeputation());
		dataMap.put("deputationPeriod", application.getDeputationPeriod());
		dataMap.put("debarredAllotment", application.getDebarred());
		dataMap.put("debarredUpto", application.getDebarredUptoDate()!=null?java.sql.Date.valueOf(application.getDebarredUptoDate()):null);
		dataMap.put("ownHouse", application.getOwnHouse());
		dataMap.put("houseDetails", application.getParticularsOfHouse());
		dataMap.put("houseBuildingAdvance", application.getHouseBuildingAdvance());
		dataMap.put("loanYear", application.getLoanYear());
		dataMap.put("houseConstructed", application.getHouseConstructed());
		dataMap.put("houseLocation", application.getHouseLocation());
		dataMap.put("presentAddress", application.getPresentAddress());
		dataMap.put("deptQuarter", application.getDeptHasQuarter());
		dataMap.put("deptQuarterReason", application.getReasonDeptQuarter());
		dataMap.put("generatedOn", Timestamp.valueOf(application.getEntrydate()));
				//java.sql.Date.valueOf(application.getEntrydate().toInstant()
                //.atZone(ZoneId.systemDefault())
                //.toLocalDate()));

		list.add(dataMap);
		return list;
	}

	public void downloadForm(HttpServletResponse response, Applications application) throws JRException, IOException {
	    UUID formCode = UUID.fromString(application.getFormUpload());
	    FormPath formPath = formPathRepo.findByFormCode(formCode);

	    String path = formPath.getPath();
	    File file = new File(path);

	    // Check if file exists
	    if (!file.exists()) {
	        throw new IOException("Form not found");
	    }

	    // Canonical path check — ensure no path traversal (e.g., symlink tricks)
	    String canonicalDbPath = file.getCanonicalPath();
	    String normalizedPath = file.getAbsoluteFile().toPath().normalize().toString();
	    if (!canonicalDbPath.equals(normalizedPath)) {
	        throw new SecurityException("Possible path traversal attempt detected.");
	    }

	    // Set appropriate content type based on extension
	    String extension = formServices.getExtensionByStringHandling(path);
	    switch (extension.toLowerCase()) {
	        case "pdf":
	            response.setContentType("application/pdf");
	            break;
	        case "jpg":
	        case "jpeg":
	            response.setContentType("image/jpeg");
	            break;
	        case "png":
	            response.setContentType("image/png");
	            break;
	        default:
	            response.setContentType("application/octet-stream");
	    }

	    response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

	    // Stream file content to response
	    try (FileInputStream fileInputStream = new FileInputStream(file);
	         OutputStream responseOutputStream = response.getOutputStream()) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            responseOutputStream.write(buffer, 0, bytesRead);
	        }
	        responseOutputStream.flush();
	    }
	}
	
	@Transactional
	public void generateAllotmentOrder(HttpServletResponse response, Applications application, Allotments allotment, Quarters quarter, String letterNo, String memoNo) throws JRException, IOException {
		
		List<Map<String, Object>> data = getAllotmentData(application, allotment, quarter, letterNo, memoNo);
		
		File file = ResourceUtils.getFile("classpath:Allotment.jrxml");

		JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("reportParameter", "AllotmentGADB");
		
		String outputPath = allotment.getFilename();
		//String outputPath = Paths.get(ordersDir, letterNo + ".pdf").toString();
			    
		// Fill Jasper report
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
		// Export report
		JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
		
		
	}
	
	List<Map<String, Object>> getAllotmentData(Applications application, Allotments allotment, Quarters quarter, String letterNo, String memoNo) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> dataMap = new HashMap<>();
		
		dataMap.put("generatedOn", new Timestamp(System.currentTimeMillis()));
		dataMap.put("letterNo", letterNo);
		dataMap.put("quarterNo", allotment.getQuarterNo()+" "+quarter.getQuarterName());
		dataMap.put("quarterType", coreService.getQuarterType(quarter.getQuarterTypeCode()));
		dataMap.put("quarterLocation", quarter.getLocation());
		dataMap.put("name", application.getName());
		dataMap.put("designation", application.getDesignation());
		dataMap.put("dept", application.getDepartmentOrDirectorate()+", "+application.getOfficeAddress());
		dataMap.put("memoNo", memoNo);
		dataMap.put("memoDate", null);
		dataMap.put("isOfficer", application.getService().equals("Others")?0:1);
		
		list.add(dataMap);
		return list;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void downloadAllotmentOrder(HttpServletResponse response, Applications application, Allotments allotment) throws JRException, IOException {

	    String path = allotment.getFilename();
	    File file = new File(path);
	    System.out.println("path: "+path);
	    // Check if file exists
	    if (!file.exists()) {
	    	FormPath form= formPathRepo.findByFormCode(UUID.fromString(path));
	    	if(form.getPath()==null || form.getPath().length()==0)
	    		throw new IOException("Order not found");
	    	else
	    		path = form.getPath();
	    }

	    // Path traversal protection
	    String canonicalPath = file.getCanonicalPath();
	    String normalizedPath = file.getAbsoluteFile().toPath().normalize().toString();
	    if (!canonicalPath.equals(normalizedPath)) {
	        throw new SecurityException("Possible path traversal attempt detected.");
	    }

	    // Set appropriate content type
	    String extension = formServices.getExtensionByStringHandling(path);
	    switch (extension.toLowerCase()) {
	        case "pdf":
	            response.setContentType("application/pdf");
	            break;
	        case "jpg":
	        case "jpeg":
	            response.setContentType("image/jpeg");
	            break;
	        case "png":
	            response.setContentType("image/png");
	            break;
	        default:
	            response.setContentType("application/octet-stream");
	    }

	    // Set header to prompt file download
	    response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

	    // Stream file to response output
	    try (FileInputStream fileInputStream = new FileInputStream(file);
	         OutputStream responseOutputStream = response.getOutputStream()) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            responseOutputStream.write(buffer, 0, bytesRead);
	        }
	        responseOutputStream.flush();
	    }
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void downloadApplicantLetter(HttpServletResponse response, Applications application, Allotments allotment) throws JRException, IOException {

	    String letterCode = allotment.getApplicantLetter();
	    FormPath formPath = formPathRepo.findByFormCode(UUID.fromString(letterCode));
	    String path = formPath.getPath();
	    File file = new File(path);

	    // Check if file exists
	    if (!file.exists()) {
	        throw new IOException("Letter not found");
	    }

	    // Path traversal protection
	    String canonicalPath = file.getCanonicalPath();
	    String normalizedPath = file.getAbsoluteFile().toPath().normalize().toString();
	    if (!canonicalPath.equals(normalizedPath)) {
	        throw new SecurityException("Possible path traversal attempt detected.");
	    }

	    // Set proper content type
	    String extension = formServices.getExtensionByStringHandling(path);
	    switch (extension.toLowerCase()) {
	        case "pdf":
	            response.setContentType("application/pdf");
	            break;
	        case "jpg":
	        case "jpeg":
	            response.setContentType("image/jpeg");
	            break;
	        case "png":
	            response.setContentType("image/png");
	            break;
	        default:
	            response.setContentType("application/octet-stream");
	    }

	    // Set download prompt header
	    response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

	    // Stream file content to response
	    try (FileInputStream fileInputStream = new FileInputStream(file);
	         OutputStream responseOutputStream = response.getOutputStream()) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            responseOutputStream.write(buffer, 0, bytesRead);
	        }
	        responseOutputStream.flush();
	    }
	}
	
	public void downloadDocument(HttpServletResponse response, UUID documentCode) throws JRException, IOException {
	    FormPath formPath = formPathRepo.findByFormCode(documentCode);
	    String path = formPath.getPath();

	    File file = new File(path);

	    // Check if file exists
	    if (!file.exists()) {
	        throw new IOException("Document not found");
	    }

	    // Path traversal protection
	    String canonicalPath = file.getCanonicalPath();
	    String normalizedPath = file.getAbsoluteFile().toPath().normalize().toString();
	    if (!canonicalPath.equals(normalizedPath)) {
	        throw new SecurityException("Possible path traversal attempt detected.");
	    }

	    // Set proper content type
	    String extension = formServices.getExtensionByStringHandling(path);
	    switch (extension.toLowerCase()) {
	        case "pdf":
	            response.setContentType("application/pdf");
	            break;
	        case "jpg":
	        case "jpeg":
	            response.setContentType("image/jpeg");
	            break;
	        case "png":
	            response.setContentType("image/png");
	            break;
	        default:
	            response.setContentType("application/octet-stream");
	    }

	    // Set response header for download
	    response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

	    // Stream file to response output
	    try (FileInputStream fileInputStream = new FileInputStream(file);
	         OutputStream responseOutputStream = response.getOutputStream()) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            responseOutputStream.write(buffer, 0, bytesRead);
	        }
	        responseOutputStream.flush();
	    }
	}
	
	public byte[] generateWaitingListReport() throws JRException {
        InputStream reportStream = getClass().getResourceAsStream("/WL.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 2. Sample data
        List<Map<String, Object>> list = new ArrayList<>();
     // First map
        Map<String, Object> map1 = new HashMap<>();
        map1.put("nameDesignation", "Alice, LDA");
        map1.put("department", "Education");
        map1.put("scaleOfPay", "3500-4500");
        map1.put("appliedDate", "01-01-2020");
        map1.put("retirementDate", "01-01-2020");
        list.add(map1);

        // Second map
        Map<String, Object> map2 = new HashMap<>();
        map2.put("nameDesignation", "Bob, UDA");
        map2.put("department", "Election");
        map2.put("scaleOfPay", "3500-4500");
        map2.put("appliedDate", "01-01-2020");
        map2.put("retirementDate", "01-01-2020");
        list.add(map2);

        // 3. Set parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", "Draft Waiting List");
        params.put("generatedOn", new Date());
        params.put("TableDataSource", new JRBeanCollectionDataSource(list));

        // 4. Fill and export
        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
        return JasperExportManager.exportReportToPdf(print);
    }
}
