package sad.storereg.repo.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sad.storereg.models.master.YearRange;

@Repository
public interface YearRangeRepository extends JpaRepository<YearRange, Integer>{

}
