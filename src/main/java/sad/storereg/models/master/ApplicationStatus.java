package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="app_status", schema="master")
public class ApplicationStatus {
	
	@Id
	@Column(name="status_code")
	private Integer statusCode;
	
	private String status;
	
	private String description;
	
	private String action;

}
