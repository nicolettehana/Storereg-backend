package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarter_physical_status", schema = "master")
@Data
public class QuarterPhysicalStatus {
	
	@Id
    @Column(name = "physical_status_code")
    private Short physicalStatusCode;

    @Column(name = "physical_status", nullable = false)
    private String physicalStatus;

}
