package sad.storereg.models.master;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="app_levels", schema ="master")
public class ApplicationLevels {
	
	@Id
	private Integer level;
	
	private String role;

}
