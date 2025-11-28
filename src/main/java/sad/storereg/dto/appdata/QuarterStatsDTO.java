package sad.storereg.dto.appdata;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuarterStatsDTO {
	
	private String quarterTypeCode;
	
    private long occupied;
    
    private long vacant;
    
    private long total;
    
    private long reserved;
    
    private long unusable;
    
    private long majorRepair;

}
