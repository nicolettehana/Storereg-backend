package sad.storereg.models.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "category", schema = "master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
    private Long id;
	
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, name="type")
    private String stockType;
    
    @Column(name = "office_code", nullable = false)
    private Integer officeCode;

}
