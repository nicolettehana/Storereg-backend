package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;

@Data
public class ItemPurchaseDTO {
	
	private String itemName;
    private List<SubItemPurchaseDTO> subItems;

}
