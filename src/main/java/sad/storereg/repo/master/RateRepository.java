package sad.storereg.repo.master;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Rate;

public interface RateRepository extends JpaRepository<Rate, Long>{
	
	Page<Rate> findByYearRange_Id(Integer yearRangeId, Pageable pageable);
	Page<Rate> findByCategory_Code(String code, Pageable pageable);
	Page<Rate> findByCategory_CodeAndYearRange_Id(String code, Integer yearRangeId, Pageable pageable);
	//Optional<Rate> findByObjectTypeAndObjectIdAndYearRange_Id(String objectType, Long objectId, Integer yearRangeId);
	Optional<Rate> findByItem_IdAndSubItemIsNullAndYearRange_Id(
	        Long itemId,
	        Integer yearRangeId
	);
	Optional<Rate> findByItem_IdAndSubItem_IdAndYearRange_Id(
	        Long itemId, Long subItemId,
	        Integer yearRangeId
	);



}
