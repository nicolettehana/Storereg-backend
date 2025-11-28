package sad.storereg.models.master;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "offices", schema = "master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offices {

	@Id
    @Column(name = "office_code")
    private Integer officeCode;

    @Column(name = "office_name", nullable = false)
    private String officeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", referencedColumnName = "dept_code", nullable = false)
    @JsonIgnore
    private Departments department;
}
