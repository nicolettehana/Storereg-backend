package sad.storereg.dto.appdata;

import java.time.LocalDate;

import lombok.Data;

@Data
public class WaitingListEntryDTO {

	private String appNo;
	
	private int slNo;
	
	private String firstName;
	
	private String designation;
	
	private String department;
	
	private String officeAddress;
	
	private String scaleOfPay;
	
	private LocalDate dateOfRetirement;
	
	private LocalDate entrydate;
}
