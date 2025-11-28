package sad.storereg.models.appdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import sad.storereg.dto.appdata.ActionDTO;

@Entity
@Data
@Table(name="applications", schema ="appdata")
public class Applications {

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;
	
	@Column(name="app_no", unique = true)
	private String appNo;
	
	@Required(message="name is required")
	@Column(name="name")
	private String name;
	
	@Required(message="basicPay is required")
	@Column(name="basic_pay")
	private Integer basicPay;
	
	@Required(message="scaleOfPay is required")
	@Column(name="scale_of_pay")
	private String scaleOfPay;
	
	@Required(message="designation is required")
	private String designation;
	
	@Required(message="departmentOrDirectorate is required")
	@Column(name="department_directorate")
	private String departmentOrDirectorate;
	
	@Required(message="officeAddress is required")
	@Column(name="office_address")
	private String officeAddress;
	
	@Column(name="office_telephone")
	private String officeTelephone;
	
	@Required(message="dateEmployed is required")
	@Column(name="date_employed")
	private LocalDate dateEmployed;
	
	@Required(message="dateOfRetirement is required")
	@Column(name="date_of_retirement")
	private LocalDate dateOfRetirement;
	
	@Required(message="gender is required")
	@Pattern(regexp = "M|F|O", message = "Gender must be 'M', 'F', or 'O'")
	private String gender;
	
	@Column(name="marital_status")
	private String maritalStatus;
	
	@Required(message="employmentStatus is required")
	@Column(name="employmentStatus")
	@Pattern(regexp = "Temporary|Quasi Permanent|Permanent", message = "Gender must be 'Temporary', 'Quasi Permanent, or 'Permanent'")
	private String employmentStatus;
	
	@Column(name="spouse_accommodation")
	@Pattern(regexp = "Y|N", message = "Spouse must be 'Y', or 'N'")
	private String spouseAccommodation;
	
	@Column(name="accommodation_details")
	private String accommdationDetails;
	
	@Required(message="service is required")
	private String service;
	
	@Column(name="other_services_details")
	private String otherServicesDetails;
	
	@Required(message="centralDeputation is required")
	@Column(name="central_deputation")
	@Pattern(regexp = "Y|N", message = "centralDeputation must be 'Y', or 'N'")
	private String centralDeputation;
	
	@Column(name="deputation_period")
	private String deputationPeriod;
	
	@Required(message="debarred is required")
	@Pattern(regexp = "Y|N", message = "Debarred must be 'Y', or 'N'")
	private String debarred;
	
	@Column(name="debarred_upto_date")
	private LocalDate debarredUptoDate;
	
	@Required(message="ownHouse is required")
	@Column(name="own_house_shillong")
	@Pattern(regexp = "Y|N", message = "ownHouse must be 'Y', or 'N'")
	private String ownHouse;
	
	@Column(name="particulars_of_house")
	private String particularsOfHouse;
	
	@Required(message="houseBuildingAdvance is required")
	@Column(name="house_building_advance")
	private String houseBuildingAdvance;
	
	@Column(name="loan_year")
	private Integer loanYear;
	
	@Column(name="house_constructed")
	private String houseConstructed;
	
	@Column(name="house_location")
	private String houseLocation;
	
	@Required(message="presentAddress is required")
	@Column(name="present_address")
	private String presentAddress;
	
	@Required(message="deptHasQuarter is required")
	@Column(name="dept_has_quarter")
	@Pattern(regexp = "Y|N", message = "deptHasQuarter must be 'Y', or 'N'")
	private String deptHasQuarter;
	
	@Column(name="reason_dept_quarter")
	private String reasonDeptQuarter;
	
	private LocalDateTime entrydate;
	
	@Column(name="upload")
	private String formUpload;
	
	@Column(name="upload_timestamp")
	private LocalDateTime uploadTimestamp;
	
	private Integer level;

	@Column(name="app_status")
	private Integer appStatus;
	
	@Column(name="user_code")
	private Long userCode;
	
	@Column(name="esigned_timetstamp")
	private LocalDateTime esignedTimestamp;
	
	@Column(name="allotment_id")
	private Long allotmentId;
	
	@Column(name="vacate_status")
	private Integer vacateStatus;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(name="wl_code")
	private Integer wlCode;
	
	@Column(name="wl_sl_no")
	private Integer wlSlNo;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(name="wl_version")
	private Integer wlVersion;
	
	@Column(name="wl_name")
	private String waitingListName;
	
	@Transient // This indicates that the field should not be persisted
    private List<ActionDTO> actions; // or SomeActionClass[] actions;
}
