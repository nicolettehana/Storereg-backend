package sad.storereg.dto.appdata;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class PurchaseCreateDTO {
	
	private String remarks;
    private LocalDate purchaseDate;
    private Long firmId;
    private Double totalCost;
    private LocalDate issueDate;
    private String issueTo;
    private String fileNo;

    private List<ItemCreatePurchaseDTO> items;

}
