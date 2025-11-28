package sad.storereg.repo.appdata;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sad.storereg.models.appdata.ApplicationsWaitingList;

public interface ApplicationsWaitingListRepository extends JpaRepository<ApplicationsWaitingList, Long>{

	Optional<ApplicationsWaitingList> findTopByWaitingListCodeOrderByWaitingListNoDesc(Integer waitingListCode);
	
	List<ApplicationsWaitingList> findAllByWaitingListCodeOrderByWaitingListNoAsc(Integer waitingListCode);
	
	List<ApplicationsWaitingList> findAllByWaitingListCodeAndIsApprovedEqualsOrderByWaitingListNoAsc(Integer waitingListCode, Integer isApproved);
	
	List<ApplicationsWaitingList> findAllByWaitingListCodeAndIsApprovedIsNullOrderByWaitingListNoAsc(Integer waitingListCode);
	
	Optional<ApplicationsWaitingList> findByAppNo(String appNo);
	
	List<ApplicationsWaitingList> findByWaitingListCodeAndWaitingListNoGreaterThanOrderByWaitingListNo(Integer waitinfListCode, Integer waitingListNo);
	
	List<ApplicationsWaitingList> findByWaitingListCodeAndWaitingListNoGreaterThanEqualOrderByWaitingListNo(Integer waitinfListCode, Integer waitingListNo);
	
	List<ApplicationsWaitingList> findByWaitingListCode(Integer waitingListCode);
	
	List<ApplicationsWaitingList> findByWaitingListCodeAndIsApproved(Integer waitingListCode, Integer isApproved);
	
	@Query("SELECT a FROM ApplicationsWaitingList a WHERE a.waitingListCode = :wlCode AND (a.isApproved IS NULL OR a.isApproved = 0) ORDER BY a.waitingListNo ASC")
	List<ApplicationsWaitingList> findByWaitingListCodeAndIsApprovedNullOrZero(@Param("wlCode") Integer wlCode);
}
