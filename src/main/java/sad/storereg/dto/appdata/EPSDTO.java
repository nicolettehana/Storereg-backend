package sad.storereg.dto.appdata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EPSDTO {
	
	@NotNull(message="requestId is required")
	public Long requestId;
	
	@NotBlank(message="status is required")
	public String status;

}