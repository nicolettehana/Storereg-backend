package sad.storereg.repo.master;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sad.storereg.dto.appdata.CategoryCountDTO;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.Quarters;

public interface ItemRepository extends JpaRepository<Item, Long>{
	
	Page<Item> findAllByCategory_Code(String category, Pageable pageable);
	
	List<Item> findAllByCategory_Code(String category);
	
	@Query("""
	        SELECT new sad.storereg.dto.appdata.CategoryCountDTO(
	            i.category.name,
	             SUM(
            CASE 
                WHEN size(i.subItems) = 0 THEN 1      
                ELSE size(i.subItems)                
            END
        )
	        )
	        FROM Item i
	        GROUP BY i.category.name
	    """)
	    List<CategoryCountDTO> getCategoryCounts();
	
	@Query("""
		    SELECT SUM(
		        CASE 
		            WHEN size(i.subItems) = 0 THEN 1
		            ELSE size(i.subItems)
		        END
		    )
		    FROM Item i
		""")
		Long getAbsoluteTotal();


}
