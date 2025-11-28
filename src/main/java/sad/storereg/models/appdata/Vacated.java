package sad.storereg.models.appdata;

import java.time.LocalDate;
import java.util.Date;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "vacated", schema = "appdata")
@Data
public class Vacated {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Required
	@Column(name="vacate_date")
	private LocalDate vacateDate;
	
	@Required
	@Column(name="app_no")
	private String appNo;

	@Column(name="allotment_id")
	private Long allotmentId;
	
	@Column(name="by_user")
	private String byUser;
	
	private Date entrydate;
	
	@Column(name="order_path")
	private String orderPath;
	
	@Required
	@Column(name="quarter_no")
	private String quarterNo;
}
