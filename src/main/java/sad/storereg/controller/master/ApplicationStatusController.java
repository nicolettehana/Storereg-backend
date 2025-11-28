package sad.storereg.controller.master;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationStatus;
import sad.storereg.services.appdata.ApplicationsServices;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class ApplicationStatusController {
	
	private final ApplicationsServices appService;
	
	@GetMapping
	public List<ApplicationStatus> get(@AuthenticationPrincipal User user) {

		return appService.getApplicationStatus();
	}

}
