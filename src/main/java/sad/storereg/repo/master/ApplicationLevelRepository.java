package sad.storereg.repo.master;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.ApplicationLevels;

public interface ApplicationLevelRepository extends JpaRepository<ApplicationLevels, Integer>{

	ApplicationLevels findByRole(String role);
}
