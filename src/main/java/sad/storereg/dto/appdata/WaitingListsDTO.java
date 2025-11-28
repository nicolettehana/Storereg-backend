package sad.storereg.dto.appdata;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class WaitingListsDTO {

	private String appNo;
	
	private LocalDate entrydate;
	
	private Integer waitingListCode;
	
	private Integer waitingListNo;
	
	private String name;
	
	private String designation;
	
	private String department;
	
	private String scaleOfPay;
	
	private LocalDate dateOfRetirement;
	
	private Integer basicPay;
	
	private String departmentOrDirectorate;
	
	private String officeAddress;
	
	private String officeTelephone;
	
	private String maritalStatus;
	
	private String employmentStatus;
	
	private String spouseAccommodation;
	
	private String accommdationDetails;
	
	private String service;
	
	private String otherServicesDetails;
	
	private String centralDeputation;
	
	private String deputationPeriod;
	
	private String debarred;
	
	private LocalDate debarredUptoDate;
	
	private String ownHouse;
	
	private String particularsOfHouse;
	
	private String houseBuildingAdvance;
	
	private Integer loanYear;
	
	private String houseConstructed;
	
	private String houseLocation;
	
	private String presentAddress;
	
	private String deptHasQuarter;
	
	private String reasonDeptQuarter;
	
	private Date approvalTimestamp;
	
	private String letterNo;
	
	private UUID docCode;	
	
	private LocalDateTime uploadTimestamp;
	
    private List<ActionDTO> actions; 
	
}
