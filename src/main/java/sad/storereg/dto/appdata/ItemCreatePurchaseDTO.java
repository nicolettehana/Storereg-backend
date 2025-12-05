package sad.storereg.dto.appdata;

import lombok.Data;

@Data
public class ItemCreatePurchaseDTO {

	private String categoryCode;
    private Long itemId;
    private Long subItemId;
    private Integer unitId;
    private Integer quantity;
    private Double rate;
    private Double amount;
}
