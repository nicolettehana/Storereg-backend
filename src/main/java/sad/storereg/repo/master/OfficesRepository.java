package sad.storereg.repo.master;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Offices;

public interface OfficesRepository extends JpaRepository<Offices, Integer>{
	
	Optional<Offices> findByOfficeCode(Integer officeCode);

}
