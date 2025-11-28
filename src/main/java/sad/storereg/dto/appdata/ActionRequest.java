package sad.storereg.dto.appdata;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActionRequest {
	
	@NotBlank(message = "Application no. (appNo) is requried")
	public String appNo;
	
	public String remarks;
	
	public Integer actionCode;
	
	public Integer waitingList;
	
	public String quarterNo;
	
	public LocalDate occupationDate;
	
	public Integer wlSlNo;
	
	public String letterNo;

	public String memoNo;
	
	public String reason;
	
	public UUID docCode;
	
	public Integer isEproposal;
}
