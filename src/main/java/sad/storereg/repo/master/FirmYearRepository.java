package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sad.storereg.dto.appdata.CategoryCountDTO;
import sad.storereg.models.master.FirmYear;

public interface FirmYearRepository extends JpaRepository<FirmYear, Long>{

	Page<FirmYear> findByYearRange_IdAndOfficeCode(Integer yearRangeId, Integer officeCode, Pageable pageable);

    Page<FirmYear> findByYearRange_IdAndCategory_CodeAndOfficeCode(Integer yearRangeId, String categoryCode, Integer officeCode, Pageable pageable);
    
    List<FirmYear> findByFirm_Id(Long firmId);
    
    Optional<FirmYear> findByYearRange_IdAndCategory_CodeAndFirm_Id(Integer yearRangeId, String categoryCode, Long firmId);
    
    @Query("""
            SELECT new sad.storereg.dto.appdata.CategoryCountDTO(
                c.name,
                c.code,
                COUNT(DISTINCT fy.firm.id)
            )
            FROM FirmYear fy
            JOIN fy.category c
            JOIN fy.yearRange yr
            WHERE yr.id = :yearRangeId AND yr.officeCode= :officeCode
            GROUP BY c.name, c.code
        """)
        List<CategoryCountDTO> findCategoryCountsByYearRangeAndOfficeCode(
                @Param("yearRangeId") Long yearRangeId, @Param("officeCode") Integer officeCode
        );


        @Query("""
            SELECT fy
            FROM FirmYear fy
            JOIN fy.firm f
            JOIN fy.yearRange yr
            WHERE yr.id = :yearRangeId AND yr.officeCode = :officeCode
              AND LOWER(f.firm) LIKE LOWER(CONCAT('%', :firmName, '%'))
        """)
        Page<FirmYear> findByOfficeCodeAndYearRangeIdAndFirmNameLike(
        		@Param("officeCode") Integer officeCode,
                @Param("yearRangeId") Integer yearRangeId,
                @Param("firmName") String firmName,
                Pageable pageable
        );


}
