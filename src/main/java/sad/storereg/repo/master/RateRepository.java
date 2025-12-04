package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	@Query("SELECT r FROM Rate r WHERE r.item.id = :itemId " +
		       "AND ((:subItemId IS NULL AND r.subItem IS NULL) OR r.subItem.id = :subItemId) " +
		       "AND r.yearRange.id = :yearRangeId")
		List<Rate> findRatesByItemAndOptionalSubItem(
		        @Param("itemId") Long itemId,
		        @Param("subItemId") Long subItemId,
		        @Param("yearRangeId") Integer yearRangeId);



}
