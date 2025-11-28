package sad.storereg.controller.appdata;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.OccupantDTO;
import sad.storereg.dto.master.QuarterRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Quarters;
import sad.storereg.services.appdata.OccupantServices;
import sad.storereg.services.master.QuartersMasterService;

@RestController
@RequestMapping("/occupants")
@RequiredArgsConstructor
public class OccupantsController {

	private final OccupantServices occupantService;
	private final QuartersMasterService quartersMasterService;
	
	@Auditable
	@Transactional
	@PostMapping("/add")
	public ResponseEntity<Map<String, String>> addOccupant(@Valid @RequestBody QuarterRequestDTO request,
			@AuthenticationPrincipal User user){
		try {
			Map<String, String> map = new HashMap<>();
			request.setQuarterStatus(1);
			occupantService.addOccupant(request, user.getId());
			map.put("detail", "Occupant added successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
			
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to add occupant",ex);
		}
	}
	
	@Auditable
	@Transactional
	@PostMapping("/add-quarter-occupant")
	public ResponseEntity<Map<String, String>> addQuarterOccupant(@Valid @RequestBody QuarterRequestDTO request,
			@AuthenticationPrincipal User user){
		try {
			Map<String, String> map = new HashMap<>();
			 //First set it as vacant, when occupant is added in occupantService, the status changes to 1
			
			//quartersMasterService.addQuarter(quarter);
			quartersMasterService.saveOrUpdateQuarterWithDetails(request);
			occupantService.addOccupant(request, user.getId());
			map.put("detail", "Occupant and quarter added successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
			
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to add occupant",ex);
		}
	}
}
