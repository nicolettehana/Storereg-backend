package sad.storereg.models.appdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "vacate_request", schema = "appdata")
@Data
public class VacateRequest {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@ManyToOne
    @JoinColumn(name = "allotment_id", nullable = false)
    private Allotments allotment;
	
	@Column(name = "request_date", nullable = false, updatable = false)
    private LocalDateTime requestDate = LocalDateTime.now();
	
	@Column(name = "vacate_date", nullable = false)
    private LocalDate vacateDate;
	
	private String status;

	@Column(name="approved_date")
	private Date approvedDate;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(name="letter_code")
	private UUID letterCode;
	
	private String username;
	
	private String remarks;
	
	@OneToMany(mappedBy = "vacateRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VacateDocument> documents;

}
