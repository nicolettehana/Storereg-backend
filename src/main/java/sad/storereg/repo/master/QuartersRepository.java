package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sad.storereg.dto.master.QuarterRequestDTO;
import sad.storereg.models.master.Quarters;

public interface QuartersRepository extends JpaRepository<Quarters, String>{
	
	List<Quarters> findAllByQuarterTypeCodeAndQuarterStatusEquals(String quarterCode, Integer quarterStatus);
	
	Page<Quarters> findAllByQuarterTypeCode(String quarterCode, Pageable pageable);
	
	Page<Quarters> findAllByQuarterTypeCodeAndIsEnabledEquals(String quarterCode, Integer isEnabled, Pageable pageable);
	
	Optional<Quarters> findByQuarterNo(String quarterNo);
	
	List<Quarters> findAllByQuarterTypeCodeAndInAllotmentListAndIsEnabledEquals(String quarterCode, Integer inAllotmentList, Integer isEnbaled);
	
	List<Quarters> findAllByQuarterTypeCodeAndInAllotmentListAndQuarterStatusAndIsEnabledEquals(String quarterCode, Integer inAllotmentList, Integer status, Integer isEnbaled);
	
	Page<Quarters> findAllByOrderByQuarterNoAsc(Pageable pageable);
	
	Page<Quarters> findByQuarterTypeCodeNotInAndIsEnabledEqualsOrderByQuarterNoAsc(List<String> excludedCodes, Integer isEnabled, Pageable pageable);
	
	Page<Quarters> findAllByQuarterStatusOrQuarterStatusOrderByQuarterNoAsc(Integer quarterStatus, Integer quarterStatus2, Pageable pageable);
	
	Page<Quarters> findAllByQuarterStatusOrderByQuarterNoAsc(Integer quarterStatus, Pageable pageable);
	
	Page<Quarters> findAllByQuarterStatusAndIsEnabledEqualsOrderByQuarterNoAsc(Integer quarterStatus, Integer isEnabled, Pageable pageable);
	
	Page<Quarters> findAllByQuarterStatusAndQuarterTypeCodeEqualsOrderByQuarterNoAsc(Integer quarterStatus, String quarterTypeCode, Pageable pageable);
	
	Page<Quarters> findAllByQuarterStatusAndQuarterTypeCodeEqualsAndIsEnabledEqualsOrderByQuarterNoAsc(Integer quarterStatus, String quarterTypeCode, Integer isEnabled, Pageable pageable);
	
	List<Quarters> findAllByQuarterStatusInAndInAllotmentListOrderByQuarterNoAsc(List<Integer> quarterStatus, Integer inAllotmentList);
	
	Page<Quarters> findByQuarterNoContainingIgnoreCase(String quarterNo, Pageable pageable);
	
	Page<Quarters> findByQuarterNoContainingIgnoreCaseAndIsEnabledEquals(String quarterNo, Integer isEnabled, Pageable pageable);
	
	Page<Quarters> findByQuarterNoContainingIgnoreCaseAndQuarterStatus(
		    String quarterNo, Integer quarterStatus, Pageable pageable);
	
	Page<Quarters> findByQuarterNoContainingIgnoreCaseAndQuarterStatusAndIsEnabledEquals(
		    String quarterNo, Integer quarterStatus, Integer isEnabled, Pageable pageable);
	
	@Query("SELECT q FROM Quarters q " +
		       "JOIN Allotments a ON q.allotmentId = a.id " +
		       "JOIN Applications app ON a.appNo = app.appNo " +
		       "WHERE app.userCode = :userCode AND q.quarterStatus=1 AND (app.vacateStatus IS NULL OR (app.vacateStatus != 17 AND app.vacateStatus != 18)) ")
			//"AND q.allotmentId NOT IN (SELECT v.allotment.id FROM VacateRequest v)")
	List<Quarters> findAvailableQuarters(@Param("userCode") Long userCode);
	
	//@Query("SELECT q FROM Quarters q LEFT JOIN Allotments a ON q.quarterNo = a.quarterNo WHERE a.occupationDate IS NOT NULL AND q.isOccupied = 1 ORDER BY q.quarterNo ASC")
	//List<Quarters> findOccupiedQuarters();
	
	//@Query("SELECT q FROM Quarters q LEFT JOIN Allotments a ON q.quarterNo = a.quarterNo WHERE (q.isOccupied = 1 AND a.occupationDate IS NULL) ORDER BY q.quarterNo ASC")
	//List<Quarters> findAllottedAndNotOccupiedQuarters();
	
	Integer countByQuarterStatus(Integer quarterStatus);
	
	@Query(value = """
		    SELECT 
		        quarter_code,
		        SUM(CASE WHEN is_occupied = 1 THEN 1 ELSE 0 END),
		        SUM(CASE WHEN (is_occupied != 1 OR is_occupied IS NULL) AND is_enabled = 1 THEN 1 ELSE 0 END),
		        COUNT(*),
		        SUM(CASE WHEN is_occupied = 5 THEN 1 ELSE 0 END),
		        SUM(CASE WHEN is_occupied = 4 THEN 1 ELSE 0 END),
		        SUM(CASE WHEN is_occupied = 3 THEN 1 ELSE 0 END)
		        
		    FROM master.quarters
		    WHERE quarter_code = :code
		    GROUP BY quarter_code
		""", nativeQuery = true)
		List<Object[]> getQuarterStatsByType(@Param("code") String code);
		

		
//		@Query("""
//			    SELECT new gad.quarters.dto.master.QuarterRequestDTO(
//			        q.quarterNo, q.quarterName, q.quarterTypeCode, q.quarterStatus, q.location,
//			        q.inAllotmentList, q.isEnabled, q.allotmentId, q.districtCode, q.blockCode,
//			        q.villageCode, q.physicalStatus, q.departmentCode, q.officeCode,
//			        d.inaugurationDate, d.assetType, d.assetDescription, d.latitude, d.longitude,
//			        d.landAssetId, d.assetCategory, d.structureType, d.builtUpArea, d.managedBy,
//			        a.name, a.designation, a.gender, al.csDecisionTimestamp, 
//			        a.departmentOrDirectorate, a.scaleOfPay, al.occupationDate, a.dateOfRetirement
//			    )
//			    FROM QuarterDetails d
//			    JOIN d.quarters q
//			    LEFT JOIN Allotments all ON q.quarterNo = all.quarterNo
//			    LEFT JOIN Applications a ON all.id = a.allotmentId
//			    WHERE d.quarterNo = :quarterNo
//			""")
			@Query("""
		    SELECT new sad.storereg.dto.master.QuarterRequestDTO(
		        q.quarterNo, q.quarterName, q.quarterTypeCode, q.quarterStatus, q.location,
		        q.inAllotmentList, q.isEnabled, q.allotmentId, q.districtCode, q.blockCode,
		        q.villageCode, q.physicalStatus, q.departmentCode, q.officeCode,
		        d.inaugurationDate, d.assetType, d.assetDescription, d.latitude, d.longitude,
		        d.landAssetId, d.assetCategory, d.structureType, d.builtUpArea, d.managedBy
		    )
		    FROM QuarterDetails d
		    JOIN d.quarters q
		    WHERE d.quarterNo = :quarterNo
			""")
			QuarterRequestDTO findQuarterFullDetailsByQuarterNo(@Param("quarterNo") String quarterNo);


}
