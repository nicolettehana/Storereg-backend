package sad.storereg.repo.appdata;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.Vacated;

public interface VacatedRepository extends JpaRepository<Vacated, Long>{
	
	Page<Vacated> findByVacateDateBetween(LocalDate fromDate, LocalDate toDate, Pageable pageable);

}
