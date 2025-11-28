package sad.storereg.repo.appdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.ApplicationsHistory;

public interface ApplicationHistoryRepository extends JpaRepository<ApplicationsHistory, Long>{

	Optional<ApplicationsHistory> findTopByAppNoOrderByEntrydateDesc(String applicationNo);
	
	Optional<ApplicationsHistory> findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(String applicationNo, Integer flowCode);
	
	List<ApplicationsHistory> findAllByAppNoOrderByEntrydateAsc(String appNo);
	
	List<ApplicationsHistory> findAllByUserCodeOrderByEntrydateDesc(Long userCode);
	
}
