package sad.storereg.repo.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.ApplicationFlow;

public interface ApplicationFlowRepository extends JpaRepository<ApplicationFlow, Integer>{
	
	List<ApplicationFlow> findAllByFromStatus(Integer fromStatus);
	
	ApplicationFlow findByFromStatusEqualsAndToStatusEquals(Integer fromStatus, Integer toStatus);
	
	ApplicationFlow findByCode(Integer flowCode);

}
