package sad.storereg.dto.master;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class QuarterOccupancyStatusDTO {
	
	private Integer occupancyStatusCode;
	
	private String occupancyStatus;

}
