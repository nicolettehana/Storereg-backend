package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Blocks;

public interface BlocksRepository extends JpaRepository<Blocks, Integer>{
	
	List<Blocks> findAllByDistrictCode(Integer districtCode);
	
	Optional<Blocks> findByBlockCode(Integer blockCode);

}
