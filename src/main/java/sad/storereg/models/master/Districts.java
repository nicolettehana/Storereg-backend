package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "districts", schema = "master")
@Data
public class Districts {
	
	@Id
    @Column(name = "lgd_code")
    private Integer lgdCode;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "short_name", length = 5)
    private String shortName;

}
