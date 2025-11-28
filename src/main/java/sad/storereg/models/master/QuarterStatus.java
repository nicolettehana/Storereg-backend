package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarter_occupancy_status", schema = "master")
@Data
public class QuarterStatus {
	
	@Id
	@Column(name="status_code")
	private Integer statusCode;
	
	@Column(name="quarter_status")
	private String quarterStatus;

}
