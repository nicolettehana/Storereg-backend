package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Villages;

public interface VillagesRepository extends JpaRepository<Villages, Integer>{
	
	List<Villages> findAllByBlockCode(Integer blockCode);
	
	Optional<Villages> findByVillageCode(Integer villageCode);

}
