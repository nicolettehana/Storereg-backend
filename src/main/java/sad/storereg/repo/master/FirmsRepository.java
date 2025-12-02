package sad.storereg.repo.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Firm;

public interface FirmsRepository extends JpaRepository<Firm, Long>{
	
	
	@EntityGraph(attributePaths = {"categories", "categories.category"})
	Page<Firm> findAll(Pageable pageable);
	

}
