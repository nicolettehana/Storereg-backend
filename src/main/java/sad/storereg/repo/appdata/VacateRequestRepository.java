package sad.storereg.repo.appdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.VacateRequest;

public interface VacateRequestRepository extends JpaRepository<VacateRequest, Long>{
	
	Page<VacateRequest> findAllByUsername(String username, Pageable pageable);
	
	VacateRequest findByAllotment_Id(Integer allotmentId);
	
	VacateRequest findByAllotment_IdAndStatus(Integer allotmentId, String status);
	
	Page<VacateRequest> findAllByStatusEquals(String status, Pageable pageable);
	
	Page<VacateRequest> findAllByStatusNot(String status, Pageable pageable);
	
	Integer countByStatusEquals(String status);

}
