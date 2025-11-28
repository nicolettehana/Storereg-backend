package sad.storereg.models.appdata;

import java.util.Date;
import java.util.UUID;

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
@Table(name="application_history", schema="appdata")
public class ApplicationsHistory {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;

	@Column(name="app_no")
	private String appNo;

	private String remarks;
	
	private Date entrydate;
	
	@Column(name="user_code")
	private Long userCode;
	
	@Column(name="flow_code")
	private Integer flowCode;

}
