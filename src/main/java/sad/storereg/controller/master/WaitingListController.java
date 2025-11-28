package sad.storereg.controller.master;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.WaitingListsDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.WaitingList;
import sad.storereg.services.appdata.WaitingListServices;

@RestController
@RequestMapping("/waiting-list")
@RequiredArgsConstructor
public class WaitingListController {
	
	private final WaitingListServices waitingListService;

	@GetMapping	
	public List<WaitingList> getWaitingList(@AuthenticationPrincipal User user) throws IOException {
		try {
			return waitingListService.getList();
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch lists", ex);
		}
	}
	
	@GetMapping("/approved/{wlCode}")
	public List<WaitingListsDTO> getApprovedWaitingListCode(@PathVariable Integer wlCode) throws IOException {
		try {
			
			return waitingListService.getApprovedWaitingListByCode(wlCode);
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch application waiting lists", ex);
		}
	}
	
	@GetMapping("/applications/{wlCode}")
	public List<WaitingListsDTO> getApplicationsWaitingListCode(@PathVariable Integer wlCode) throws IOException {
		try {
			
			return waitingListService.getApplicationsWaitingListByCode(wlCode);
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch application waiting lists", ex);
		}
	}
	
	@GetMapping("/applications")
	public List<WaitingListsDTO> getApplicationsWaitingList() throws IOException {
		try {
			return waitingListService.getApplicationsWaitingList();
			
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch application waiting lists", ex);
		}
	}
	
	@GetMapping("/previous/{applicationNo}")
	public ResponseEntity<Map<String,Object>> getPreviousWaitingList(@PathVariable String applicationNo) throws IOException {
		try {
			return new ResponseEntity<>(waitingListService.getPreviousWaitingListData(applicationNo), HttpStatus.OK);
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch previous waiting list data", ex);
		}
	}
	
}
