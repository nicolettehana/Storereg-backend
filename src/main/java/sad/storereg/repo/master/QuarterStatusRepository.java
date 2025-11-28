package sad.storereg.repo.master;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.QuarterStatus;

public interface QuarterStatusRepository extends JpaRepository<QuarterStatus, Integer>{

}
