package sad.storereg.dto.appdata;

import lombok.Data;

@Data
public class ItemCreatePurchaseDTO {

	private String categoryCode;
    private String itemId;
    private String subItemId;
    private String unitId;
    private String quantity;
    private String rate;
    private String amount;
}
