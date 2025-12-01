package sad.storereg.controller.master;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.master.Unit;
import sad.storereg.services.master.MasterDataServices;

@RestController
@RequestMapping("/unit")
@RequiredArgsConstructor
public class UnitController {

	private final MasterDataServices masterDataServices;
	
	@GetMapping
	public List<Unit> getUnits() throws IOException {
		try {
			return masterDataServices.getUnits();
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch units", ex);
		}
	}
}
