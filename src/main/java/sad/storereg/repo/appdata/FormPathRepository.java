package sad.storereg.repo.appdata;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.FormPath;

public interface FormPathRepository extends JpaRepository<FormPath, Long>{

	FormPath findByFormCode(UUID formCode);
}
