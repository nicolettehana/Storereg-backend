package sad.storereg.repo.appdata;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sad.storereg.dto.appdata.PendingAllotmentsDTO;
import sad.storereg.models.appdata.Allotments;

public interface AllotmentsRepository extends JpaRepository<Allotments, Long>{
	
	Optional<Allotments> findTopByQuarterNoAndApplicantAcceptEqualsOrderByApplicantAcceptTimestamp(String quarterNo, Integer isApproved);
	
	Optional<Allotments> findByIdAndPrevWLNotNullAndPrevWLnoNotNullOrderByEntrydate(Long id);
	
	Optional<Allotments> findByIdAndIsApprovedEqualsAndCsDecisionTimestampNull(Long id,Integer isApproved);
	
	Optional<Allotments> findByIdAndIsApprovedEquals(Long id, Integer status);
	
	Optional<Allotments> findByLetterNo(String letterNo);
	
	Optional<Allotments> findByApplicantLetter(String letterCode);
	
	//@Query("SELECT al FROM Allotments al JOIN Applications a ON al.id = a.allotmentId WHERE a.appStatus IN (4, 5, 10, 11, 13, 14)")
	//Page<Allotments> findAllotmentsByApplicationStatus(Pageable pageable);
	
	@Query("SELECT new sad.storereg.dto.appdata.PendingAllotmentsDTO(" +
		       "a.appNo, a.name, a.designation, a.departmentOrDirectorate, a.officeAddress, " +
		       "al.letterNo, al.memoNo, al.entrydate, al.isApproved, al.csDecisionTimestamp, " +
		       "al.esignUnderSecy, al.underSecyTimestamp, al.applicantAccept, al.applicantAcceptTimestamp, " +
		       "CASE " +
		       " WHEN a.level = 1 THEN 'Department' " +
		       " WHEN a.level = 2 THEN 'Department' " +
		       " WHEN a.level = 3 THEN 'Department' " +
		       " WHEN a.level = 0 THEN 'Applicant' " +
		       " WHEN a.level = 5 THEN 'Estate Officer' " +
		       " ELSE '-' END" +
		       ") " +
		       "FROM Allotments al JOIN Applications a ON al.id = a.allotmentId " +
		       "WHERE a.appStatus IN :statuses")
	Page<PendingAllotmentsDTO> findPendingAllotments(@Param("statuses") List<Integer> statuses, Pageable pageable);

	
	@Query("SELECT new sad.storereg.dto.appdata.PendingAllotmentsDTO(" +
		       "a.appNo, a.name, a.designation, a.departmentOrDirectorate, a.officeAddress, " +
		       "al.letterNo, al.memoNo, al.entrydate, al.isApproved, al.csDecisionTimestamp, " +
		       "al.esignUnderSecy, al.underSecyTimestamp, al.applicantAccept, al.applicantAcceptTimestamp, " +
		       "CASE " +
		       " WHEN a.level = 1 THEN 'Department' " +
		       " WHEN a.level = 2 THEN 'Department' " +
		       " WHEN a.level = 3 THEN 'Department' " +
		       " WHEN a.level = 0 THEN 'Applicant' " +
		       " WHEN a.level = 5 THEN 'Estate Officer' " +
		       " ELSE '-' END" +
		       ") " +
		       "FROM Allotments al JOIN Applications a ON al.id = a.allotmentId " +
		       "WHERE a.appStatus IN :statuses AND a.appNo NOT LIKE :excludedPrefix")
		Page<PendingAllotmentsDTO> findCompletedAllotments(
		    @Param("statuses") List<Integer> statuses,
		    @Param("excludedPrefix") String excludedPrefix,
		    Pageable pageable);

	
	@Query("SELECT new sad.storereg.dto.appdata.PendingAllotmentsDTO(" +
		       "a.appNo, a.name, a.designation, a.departmentOrDirectorate, a.officeAddress, " +
		       "al.letterNo, al.memoNo, al.entrydate, al.isApproved, al.csDecisionTimestamp, " +
		       "al.esignUnderSecy, al.underSecyTimestamp, al.applicantAccept, al.applicantAcceptTimestamp, " +
		       "CASE " +
		       " WHEN a.level = 1 THEN 'Department' " +
		       " WHEN a.level = 2 THEN 'Department' " +
		       " WHEN a.level = 3 THEN 'Department' " +
		       " WHEN a.level = 0 THEN 'Applicant' " +
		       " WHEN a.level = 5 THEN 'Estate Officer' " +
		       " ELSE '-' END" +
		       ") " +
		       "FROM Allotments al JOIN Applications a ON al.id = a.allotmentId " +
		       "WHERE a.appStatus IN :statuses " +
		       "AND al.entrydate >= :fromDate " +
		       "AND al.entrydate <= :toDate " +
		       "AND a.appNo NOT LIKE :excludedPrefix")
		Page<PendingAllotmentsDTO> findCompletedAllotmentsWithDateRange(
		    @Param("fromDate") LocalDateTime fromDate,
		    @Param("toDate") LocalDateTime toDate,
		    @Param("statuses") List<Integer> statuses,
		    @Param("excludedPrefix") String excludedPrefix,
		    Pageable pageable);


}
