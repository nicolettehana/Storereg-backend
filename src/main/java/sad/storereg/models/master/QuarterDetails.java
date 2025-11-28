package sad.storereg.models.master;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quarter_details", schema = "master")
@Data
public class QuarterDetails {
	
	@Id
	@Column(name="quarter_no")
	@Required
	private String quarterNo;
	
	@OneToOne
    @JoinColumn(name = "quarter_no", referencedColumnName = "quarterNo", insertable = false, updatable = false)
    private Quarters quarters;
	
	@Column(name="inauguration_date")
	private LocalDate inaugurationDate;

	@Column(name="asset_type")
	private String assetType;
	
	@Column(name="asset_description")
	private String assetDescription;
	
	@Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;
    
    @Column(name = "land_asset_id")
    private String landAssetId;
    
    @Column(name = "asset_category")
    private String assetCategory;
    
    @Column(name = "structure_type")
    private String structureType;
    
    @Column(name = "built_up_area")
    private String builtUpArea;
    
    @Column(name="managed_by")
    private String managedBy;
}
