package sad.storereg.models.master;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="app_flow",schema="master")
public class ApplicationFlow {
	
	@Id
	private Integer code;
	
	private Integer fromStatus;
	
	private Integer toStatus;
	
	private Integer nextLevel;
	
	private String description;

}
