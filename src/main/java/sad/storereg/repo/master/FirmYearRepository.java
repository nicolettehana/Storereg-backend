package sad.storereg.repo.master;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.FirmYear;

public interface FirmYearRepository extends JpaRepository<FirmYear, Long>{

	Page<FirmYear> findByYearRange_Id(Integer yearRangeId, Pageable pageable);

    Page<FirmYear> findByYearRange_IdAndCategory_Code(Integer yearRangeId, String categoryCode, Pageable pageable);
    
    List<FirmYear> findByFirm_Id(Long firmId);

}
