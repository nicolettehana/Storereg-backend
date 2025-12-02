package sad.storereg.dto.appdata;

import lombok.Data;

@Data
public class SubItemPurchaseDTO {
	
	private String subItemName;
    private Integer quantity;
    private Double rate;
    private Double amount;

}
