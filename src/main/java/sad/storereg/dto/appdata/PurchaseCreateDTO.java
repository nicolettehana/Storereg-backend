package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;

@Data
public class PurchaseCreateDTO {
	
	private String remarks;
    private String purchaseDate;
    private String firmId;
    private String totalCost;

    private List<ItemCreatePurchaseDTO> items;

}
