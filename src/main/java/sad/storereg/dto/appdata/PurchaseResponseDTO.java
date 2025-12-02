package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;

@Data
public class PurchaseResponseDTO {
	
	private Long purchaseId;
    private String firmName;
    private String category;
    private List<ItemPurchaseDTO> items;
    private Double totalCost;
    private String remarks;

}
