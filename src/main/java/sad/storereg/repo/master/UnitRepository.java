package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Unit;

public interface UnitRepository extends JpaRepository<Unit, Integer>{
	
	Optional<Unit> findByOfficeCodeAndUnitOrOfficeCodeAndName(
		    Integer officeCode,
		    String unit,
		    Integer officeCode2,
		    String name
		);

	
	List<Unit> findAllByOfficeCode(Integer officeCode);

}
