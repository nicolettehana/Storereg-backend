package sad.storereg.repo.master;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Districts;

public interface DistrictsRepository extends JpaRepository<Districts, Integer>{
	
	Optional<Districts> findByLgdCode(Integer lgdCode);

}
