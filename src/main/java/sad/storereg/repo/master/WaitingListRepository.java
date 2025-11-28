package sad.storereg.repo.master;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.WaitingList;

public interface WaitingListRepository extends JpaRepository<WaitingList, Integer>{
	
	Optional<WaitingList> findByCode(Integer code);
	
}
