package sad.storereg.dto.appdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class VacateRequestDTO {
	
	private String appNo;
	
	private String quarterNo;
	
	private LocalDate vacateDate;
	
	private LocalDate requestedDate;
	
	private String status;
	
	private String remarks;
	
	private LocalDate decisionDate;
	
	private Long allotmentCode;
	
	private List<DocDTO> documents;

}
