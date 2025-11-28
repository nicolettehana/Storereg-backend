package sad.storereg.controller.appdata;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ApplicationFlowDTO;
import sad.storereg.dto.appdata.FlowDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.services.appdata.ApplicationHistoryServices;

@RestController
@RequestMapping("/app-history")
@RequiredArgsConstructor
public class ApplicationsHistoryController {
	
	private final ApplicationHistoryServices appHistService;
	
	@GetMapping(path = "/outbox")
	public List<FlowDTO> getOutbox(@AuthenticationPrincipal User user) {
		
		try {						
			return appHistService.getOutbox(user);
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			System.out.println(ex);
			throw ex;
		}
	}
	
	@GetMapping(path = "/{applicationNo}")
	public ApplicationFlowDTO getApplicationHistory(@PathVariable String applicationNo, @AuthenticationPrincipal User user) {
		
		try {			
			return appHistService.getApplicationHistory(applicationNo);
		} catch (ObjectNotFoundException | UnauthorizedException | InternalServerError ex) {
			throw ex;
		}
	}
	
	

}
