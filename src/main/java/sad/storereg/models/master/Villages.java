package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "villages", schema = "master")
@Data
public class Villages {

	@Id
    @Column(name = "village_code")
    private Integer villageCode;

    @Column(name = "name", nullable = false)
    private String villageName;
    
    @Column(name="block_code")
    private Integer blockCode;
}
