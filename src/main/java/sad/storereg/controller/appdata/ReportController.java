package sad.storereg.controller.appdata;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import sad.storereg.repo.auth.UserRepository;
import sad.storereg.services.appdata.CoreServices;
import sad.storereg.services.appdata.ReportsService;
import sad.storereg.services.auth.AuthenticationService;
import sad.storereg.services.auth.OtpService;

@RestController
@RequiredArgsConstructor
public class ReportController {
	
	private final ReportsService reportService;
	
	@GetMapping("/my-report")
    public ResponseEntity<byte[]> getWaitingListPdf() throws JRException {
    	try {
        byte[] pdf = reportService.generateWaitingListReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=waiting_list.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    	}
        catch(Exception ex) 
        {throw ex;}
    }

}
