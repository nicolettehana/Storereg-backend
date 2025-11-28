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
//@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Table(name="applications_waiting_list", schema="appdata")
public class ApplicationsWaitingList {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Column(name="appno")
	private String appNo;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String remarks;
	
	private Date entrydate;
	
	@Column(name="waiting_list_code")
	private Integer waitingListCode;
	
	@Column(name="waiting_list_no")
	private Integer waitingListNo;
	
	@Column(name="is_legacy")
	private Integer isLegacy;
	
	@Column(name="is_approved")
	private Integer isApproved;
	
	@Column(name="approved_timestamp")
	private Date approvedTimestamp;
	
	@Column(name="letter_no")
	private String letterNo;

	@Column(name="doc_code")
	private UUID docCode;
}
