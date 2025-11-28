package sad.storereg.controller.master;

import static sad.storereg.models.auth.Role.EST;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.QuarterOccupancyStatusDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.QuarterPhysicalOccupancy;
import sad.storereg.models.master.QuarterPhysicalStatus;
import sad.storereg.models.master.QuarterStatus;
import sad.storereg.models.master.QuarterTypes;
import sad.storereg.repo.master.QuarterPhysicalOccupancyRepository;
import sad.storereg.repo.master.QuarterPhysicalStatusRepository;
import sad.storereg.repo.master.QuarterStatusRepository;
import sad.storereg.repo.master.QuarterTypesRepository;

@RestController
@RequiredArgsConstructor
public class QuarterTypesController {
	
	private final QuarterTypesRepository quarterTypesRepo;
	private final QuarterPhysicalStatusRepository quarterPhysicalStatusRepo;
	private final QuarterPhysicalOccupancyRepository occupancyRepo;
	
	@GetMapping("/quarter-types")
	public List<QuarterTypes> getQuarterTypes(@AuthenticationPrincipal User user) throws IOException {
		try {
			if(user.getRole().equals(EST))
				return quarterTypesRepo.findAllByOrderByCodeAsc();
			else {
				List<String> excludedCodes = Arrays.asList("A", "F", "T", "G");
				//List<Quarter> quarters = quarterRepository.findAllByCodeNotIn(excludedCodes);
				return quarterTypesRepo.findAllByCodeNotIn(excludedCodes);
			}
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarter types", ex);
		}
	}
	
	@GetMapping("/quarter-status")
	public List<QuarterPhysicalStatus> getQuarterStatus() throws IOException {
		try {
			return quarterPhysicalStatusRepo.findAll();
			//return quarterStatusRepo.findAll();
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to quarter status", ex);
		}
	}
	
	@GetMapping("/quarter-occupancy-status/{physicalStatusCode}")
    public List<QuarterOccupancyStatusDTO> getOccupancyStatusesByPhysicalStatus(@PathVariable Integer physicalStatusCode) {
		try {
			return occupancyRepo.findOccupancyStatusesByPhysicalStatus(physicalStatusCode);
			//return occupancyRepo.findById_PhysicalStatus(physicalStatusCode);
			} 
		catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to quarter status", ex);
		}
    }

}
