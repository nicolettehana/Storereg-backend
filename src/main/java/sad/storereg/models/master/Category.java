package sad.storereg.models.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category", schema = "master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
	
	@Id
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

}
