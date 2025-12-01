package sad.storereg.controller.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Category;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.FirmCategoryRepository;
import sad.storereg.repo.master.FirmsRepository;
import sad.storereg.services.master.MasterDataServices;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
	
	private final MasterDataServices masterDataServices;
	private final FirmsRepository firmRepo;
	private final FirmCategoryRepository firmCategoryRepo;

	@GetMapping
	public List<Category> getCatagory(HttpServletRequest request, HttpServletResponse response , @AuthenticationPrincipal User user) throws IOException {
		try {
			return masterDataServices.getCategories();
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch menu", ex);
		}
	}
	
	@GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> response = new HashMap<>();
        response.put("total", firmRepo.count());
        response.put("byCategory", firmCategoryRepo.countFirmsPerCategory());

        return response;
    }

}
