package sad.storereg.models.master;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class QuarterStatusRelationId implements Serializable{
	
	@Column(name = "id")
    private Integer id;

    @Column(name = "physical_status")
    private Integer physicalStatus;

    public QuarterStatusRelationId() {}

    public QuarterStatusRelationId(Integer id, Integer physicalStatus) {
        this.id = id;
        this.physicalStatus = physicalStatus;
    }

    // equals and hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuarterStatusRelationId that)) return false;
        return Objects.equals(id, that.id) &&
               Objects.equals(physicalStatus, that.physicalStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, physicalStatus);
    }

}
