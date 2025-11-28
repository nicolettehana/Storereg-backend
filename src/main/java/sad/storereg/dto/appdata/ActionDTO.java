package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;
//import lombok.RequiredArgsConstructor;

//@RequiredArgsConstructor
@Data
public class ActionDTO {
	
	private String action;
    
	private Integer isEnabled;
    
	private Integer actionCode;
	
	private List<ActionDTO> subActions;
	
	// Constructor
    public ActionDTO(String action, Integer isEnabled, Integer actionCode) {
        this.action = action;
        this.isEnabled = isEnabled;
        this.actionCode = actionCode;
    }
    
    public ActionDTO(String action, Integer isEnabled, Integer actionCode, List<ActionDTO> subActions) {
        this.action = action;
        this.isEnabled = isEnabled;
        this.actionCode = actionCode;
        this.subActions = subActions;
    }
	
}
