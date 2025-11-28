package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarter_types", schema = "master")
@Data
public class QuarterTypes {

	@Id
	private String code;
	
	@Column(name="quarter_type")
	private String quarterType;

}
