package sad.storereg.models.appdata;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="allotment_orders", schema="appdata")
public class AllotmentOrders {

	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Column(name="allotment_id")
	private Long allotmentId;
	
	private String code;
	
	private String path;
	
	private String extension;
	
	private Integer step;
	
	private String username;
	
	private Date entrydate;
}
