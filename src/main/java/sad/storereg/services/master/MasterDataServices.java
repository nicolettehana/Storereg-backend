package sad.storereg.services.master;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Unit;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.UnitRepository;

@Service
@RequiredArgsConstructor
public class MasterDataServices {
	
	private final CategoryRepository categoryRepo;
	
	private final UnitRepository unitRepo;
	
	public List<Category> getCategories() {
		try {
				return categoryRepo.findAll();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<Unit> getUnits() {
		try {
				return unitRepo.findAll();
			}catch(Exception ex) {
			throw ex;
		}
    }

}
