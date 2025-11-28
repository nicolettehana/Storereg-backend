package sad.storereg.repo.master;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.QuarterTypes;

public interface QuarterTypesRepository extends JpaRepository<QuarterTypes, Integer>{
	
	Optional<QuarterTypes> findByCode(String code);
	
	List<QuarterTypes> findByCodeNot(String type);
	
	List<QuarterTypes> findAllByOrderByCodeAsc();
	
	List<QuarterTypes> findAllByCodeNotIn(Collection<String> codes);

}
