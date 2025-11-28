package sad.storereg.models.appdata;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vacate_documents", schema = "appdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacateDocument {
	
	@Id
    @GeneratedValue
    private Long id;
	
	@ManyToOne
    @JoinColumn(name = "vacate_request_id", nullable = true)
    private VacateRequest vacateRequest;
	
	@Column(name = "document_type", nullable = false)
    private String documentType;
	
	@Column(name = "document_code", nullable = false)
    private UUID documentCode;
	
	@Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

}
