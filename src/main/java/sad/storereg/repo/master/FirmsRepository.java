package sad.storereg.repo.master;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sad.storereg.models.master.Firm;

public interface FirmsRepository extends JpaRepository<Firm, Long>{
	
	
	@EntityGraph(attributePaths = {"categories", "categories.category"})
	Page<Firm> findAll(Pageable pageable);
	
	@Query("""
	        SELECT DISTINCT f 
	        FROM Firm f
	        JOIN FirmYear fy ON fy.firm = f
	        JOIN fy.yearRange yr
	        WHERE :year BETWEEN yr.startYear AND yr.endYear
	    """)
	    List<Firm> findAllByYear(int year);
	

}
