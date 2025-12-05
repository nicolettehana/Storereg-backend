package sad.storereg.dto.master;

import lombok.Data;

@Data
public class UnitRateDTO {

	private Integer unitId;
	
	private String unitName;
	
	private Double rate;
	
	private Long itemId;
	
	private Long subItemId;
	
	private String unit;
	
	private Integer balance;
}
