package sad.storereg.dto.appdata;

import java.time.LocalDate;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class OccupantDTO {

	private String name;
	
	private String designation;
	
	private String gender;
	
	private String quarterNo;
	
	private Integer quarterStatus;
	
	private LocalDate allotmentDate;
	
	private String deptOffice;
	
	private String payScale;
	
	private LocalDate occupationDate;
	
	private LocalDate retirementDate;
	
	//If new quarter is being added
	private String location;
	
	private String quarterName;
	
	private String quarterTypeCode;
}
