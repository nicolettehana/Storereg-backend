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
@Table(name="acceptance_rejection_letters", schema="appdata")
public class AcceptanceRejectionLetters {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Column(name="allotment_id")
	private Long allotmentId;
	
	private char code;
	
	private String path;
	
	private String extension;
	
	private String username;
	
	private Date entrydate;

}
