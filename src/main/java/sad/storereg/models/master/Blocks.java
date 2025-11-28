package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "blocks", schema = "master")
@Data
public class Blocks {
	
	@Id
    @Column(name = "block_code")
    private Integer blockCode;

    @Column(name = "block_name", nullable = false)
    private String blockName;
    
    @Column(name="district_code")
    private Integer districtCode;

}
