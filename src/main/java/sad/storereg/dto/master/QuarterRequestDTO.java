package sad.storereg.dto.master;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
//@NoArgsConstructor
@RequiredArgsConstructor
public class QuarterRequestDTO {

	//@NotBlank
    private String quarterNo;

    //@NotBlank
    private String quarterName;

    //@NotBlank
    private String quarterTypeCode;

    //@NotBlank
    private String location;

    //@NotNull
    private Integer quarterStatus;

    //@NotNull
    private Integer physicalStatus;

    private Integer inAllotmentList;
    private Integer isEnabled;
    private Integer allotmentId;
    private Integer districtCode;
    private Integer blockCode;
    private Integer villageCode;
    private Integer departmentCode;
    private Integer officeCode;
    
    // Details
    private LocalDate inaugurationDate;
    private String assetType;
    private String assetDescription;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String landAssetId;
    private String assetCategory;
    private String structureType;
    private String builtUpArea;
    private String managedBy;
    
    //Occupants details
    private String name;	
	private String designation;	
	private String gender;	
	private LocalDate allotmentDate;	
	private String deptOffice;	
	private String payScale;	
	private LocalDate occupationDate;	
	private LocalDate retirementDate;
	
	public QuarterRequestDTO(
	        String quarterNo, String quarterName, String quarterTypeCode, Integer quarterStatus, String location,
	        Integer inAllotmentList, Integer isEnabled, Integer allotmentId, Integer districtCode, Integer blockCode,
	        Integer villageCode, Integer physicalStatus, Integer departmentCode, Integer officeCode,
	        LocalDate inaugurationDate, String assetType, String assetDescription, BigDecimal latitude, BigDecimal longitude,
	        String landAssetId, String assetCategory, String structureType, String builtUpArea, String managedBy
	    ) {
	        this.quarterNo = quarterNo;
	        this.quarterName = quarterName;
	        this.quarterTypeCode = quarterTypeCode;
	        this.quarterStatus = quarterStatus;
	        this.location = location;
	        this.inAllotmentList = inAllotmentList;
	        this.isEnabled = isEnabled;
	        this.allotmentId = allotmentId;
	        this.districtCode = districtCode;
	        this.blockCode = blockCode;
	        this.villageCode = villageCode;
	        this.physicalStatus = physicalStatus;
	        this.departmentCode = departmentCode;
	        this.officeCode = officeCode;
	        this.inaugurationDate = inaugurationDate;
	        this.assetType = assetType;
	        this.assetDescription = assetDescription;
	        this.latitude = latitude;
	        this.longitude = longitude;
	        this.landAssetId = landAssetId;
	        this.assetCategory = assetCategory;
	        this.structureType = structureType;
	        this.builtUpArea = builtUpArea;
	        this.managedBy = managedBy;
	    }
}
