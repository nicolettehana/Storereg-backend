package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Category;

public interface CategoryRepository extends JpaRepository<Category, String>{
	
	Optional<Category> findByCodeOrName(String code, String name);
	Optional<Category> findByOfficeCodeAndCodeOrOfficeCodeAndName(
		    Integer officeCode,
		    String code,
		    Integer officeCode2,
		    String name
		);

	
	Optional<Category> findByCodeAndOfficeCode(String code, Integer officeCode);
	
	List<Category> findAllByStockTypeAndOfficeCode(String stockType, Integer officeCode);
	
	List<Category> findAllByOfficeCode(Integer officeCode);
	
//	@Query("""
//	        SELECT new com.example.dto.CategoryCountDTO(c.name, COUNT(f))
//	        FROM Category c
//	        LEFT JOIN c.firms f
//	        GROUP BY c.name
//	    """)
//	    List<CategoryCountDTO> countFirmsPerCategory();

}
