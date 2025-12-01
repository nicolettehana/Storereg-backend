package sad.storereg.dto.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubItemRateDTO {
	
	private Long id;
    private String name;
    private String unit;
    private Double rate;

}
