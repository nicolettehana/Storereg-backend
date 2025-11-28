package sad.storereg.repo.appdata;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.VacateDocument;

public interface VacateDocumentRepository extends JpaRepository<VacateDocument, Long>{
	
	Optional<VacateDocument> getByDocumentCode(UUID documentCode);

}
