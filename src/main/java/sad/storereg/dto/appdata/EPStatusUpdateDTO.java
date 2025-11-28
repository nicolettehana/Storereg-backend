package sad.storereg.dto.appdata;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EPStatusUpdateDTO {

	@NotNull(message="requestId is required")
	public Long requestId;
	
	@NotBlank(message="status is required")
	public String status;
	
	@NotBlank(message="fileMovementNote is required")
	public String fileMovementNote;
	
	@NotBlank(message="department is required")
	public String department;
	
	@NotBlank(message="office is required")
	public String office;
	
	@NotBlank(message="userName is required")
	public String userName;
	
	@NotBlank(message="designation is required")
	public String designation;
	
	@NotNull(message="movementDateTime is required in ISO format")
	public LocalDateTime movementDateTime;
	
}
