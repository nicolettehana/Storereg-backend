package sad.storereg.models.master;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarters", schema = "master")
@Data
public class Quarters {
	
	@Id
	@Column(name="house_no")
	@Required
	private String quarterNo;
	
	@Column(name="quarter_name")
	@Required
	private String quarterName;
	
	@Column(name="quarter_code")
	@Required
	private String quarterTypeCode;
	
	@Column(name="is_occupied")
	private Integer quarterStatus;

	@Required
	private String location;
	
	@Column(name="is_in_allotment_list")
	private Integer inAllotmentList;
	
	@Column(name="is_enabled")
	private Integer isEnabled;
	
	@Column(name="allotment_id")
	private Integer allotmentId;
	
	@Column(name="district_code")
	private Integer districtCode;
	
	@Column(name="block_code")
	private Integer blockCode;
	
	@Column(name="village_code")
	private Integer villageCode;
	
	@Column(name="physical_status")
	private Integer physicalStatus;
	
	@Column(name="department_code")
	private Integer departmentCode;
	
	@Column(name="office_code")
	private Integer officeCode;
}
