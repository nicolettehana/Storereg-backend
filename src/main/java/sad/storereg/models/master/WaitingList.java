package sad.storereg.models.master;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "waiting_list", schema = "master")
@Data
public class WaitingList {
	
	@Id
	private Integer code;
	
	private String list;
	
	private String description;

}
