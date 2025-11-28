package sad.storereg.dto.appdata;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartersDTO {
	
	private String quarterNo;
	
	private String quarterName;
	
	private String quarterTypeCode;
	
	private String status;
	
	private String name;
	
	private String designation;
	
	private String department;
	
	private LocalDate dateOfAllotment;
	
	private LocalDate dateOfOccupation;
	
	private LocalDate dateOfRetirement;
	
	private String location;
	
	private Integer isEnabled;
	
	private String payScale;
	
	private String appNo;
	
	private LocalDate vacatedDate;
	
	private String applicantLetterCode;

}
