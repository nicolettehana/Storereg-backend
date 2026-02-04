package sad.storereg.repo.master;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sad.storereg.models.master.YearRange;

public interface YearRangeRepository extends JpaRepository<YearRange, Integer>{
	
	Optional<YearRange> findByStartYearLessThanEqualAndEndYearGreaterThanEqualAndOfficeCode(int year1, int year2, int officeCode);
	
	//Optional<YearRange> findByOfficeCodeAnd

	List<YearRange> findAllByOfficeCodeOrderByStartYearDesc(Integer officeCode);
	
	boolean existsByStartYearLessThanEqualAndEndYearGreaterThanEqualAndOfficeCodeEquals(
            Integer endYear,
            Integer startYear,
            Integer officeCode
    );

	
}
