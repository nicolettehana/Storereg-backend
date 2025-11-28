package sad.storereg.dto.master;

import java.util.List;

import lombok.Data;
import sad.storereg.models.master.Offices;

@Data
public class DepartmentOfficeDTO {
	
	private Integer deptCode;

    private String deptName;

    private List<Offices> offices;

}
