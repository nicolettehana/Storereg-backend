package sad.storereg.repo.appdata;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sad.storereg.models.appdata.Applications;

public interface ApplicationsRepository extends JpaRepository<Applications, Long>{
	
	@Query("SELECT a.appNo FROM Applications a WHERE a.appNo NOT LIKE 'Old%' ORDER BY a.appNo DESC LIMIT 1")
    Optional<String> findLastApplicationNo();
	
	Optional<Applications> findByAppNo(String appNo);
	
	Optional<Applications> findByAllotmentId(Long allotmentId);
	
	Page<Applications> findAllByUserCodeAndAppStatusNot(Long userCode, Integer appStatus, Pageable pageable);
	
	Page<Applications> findAllByLevel(Integer level, Pageable pageable);
	
	Page<Applications> findAllByLevelAndAppStatus(Integer level, Integer appStatus, Pageable pageable);
	
	Optional<Applications> findTopByUserCodeAndAppStatusGreaterThanAndAppStatusLessThanOrderByEntrydateDesc(Long userCode, Integer appStatus, Integer appStatus2);
	
	List<Applications> findAllByUserCodeAndLevelAndUploadTimestampNotNull(Long userCode, Integer level);
	
	List<Applications> findAllByUserCodeAndAppStatusGreaterThanAndAppStatusLessThan(Long userCode, Integer appStatus1, Integer appStatus2);
	
	List<Applications> findAllByUserCodeAndAppStatusEqualsOrUserCodeAndAppStatusEqualsOrUserCodeAndAppStatusEquals(Long userCode, Integer appStatus, Long userCode2,  Integer appStatus1, Long userCode3, Integer appStatus2);
	
	Page<Applications> findAllByAppStatusGreaterThan(Integer appStatus, Pageable pageable);
	
	Page<Applications> findAllByAppStatusGreaterThanAndUploadTimestampGreaterThanEqualAndUploadTimestampLessThanEqual(Integer appStatus, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

}
