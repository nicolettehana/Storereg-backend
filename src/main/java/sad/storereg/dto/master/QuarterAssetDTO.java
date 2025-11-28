package sad.storereg.dto.master;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class QuarterAssetDTO {
	
	private String departmentName;
	
    private String officeName;

    private String assetType;
    
    private String assetDescription;

    private String district;
    
    private String block;
    
    private String village;
    
    private String location;

    private BigDecimal latitude;
    
    private BigDecimal longitude;

    private String landAssetId;
    
    private String assetCategory;
    
    private String structureType;
    
    private LocalDate inaugurationDate;
    
    private String builtUpArea;

    private String presentCondition;
    
    private String managedBy;

}
