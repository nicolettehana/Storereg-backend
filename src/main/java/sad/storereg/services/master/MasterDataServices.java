package sad.storereg.services.master;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.Category;
import sad.storereg.repo.master.CategoryRepository;

@Service
@RequiredArgsConstructor
public class MasterDataServices {
	
	private final CategoryRepository categoryRepo;
	
	public List<Category> getCategories() {
		try {
				return categoryRepo.findAll();
			}catch(Exception ex) {
			throw ex;
		}
    }

}
