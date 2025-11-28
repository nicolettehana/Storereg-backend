package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarter_physical_occupancy", schema = "master")
@Data
public class QuarterPhysicalOccupancy {
	
	@EmbeddedId
    private QuarterStatusRelationId id;
	
	@Column(name = "occupancy_status", nullable = false)
    private Integer occupancyStatus;

}
