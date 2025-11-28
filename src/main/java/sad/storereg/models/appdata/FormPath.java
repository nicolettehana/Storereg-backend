package sad.storereg.models.appdata;

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
@Table(name="form_path", schema="appdata")
public class FormPath {

	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Column(name="form_code")
	private UUID formCode;

	private String path;

	private String extension;
}
