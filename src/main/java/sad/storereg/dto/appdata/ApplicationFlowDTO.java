package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;

@Data
public class ApplicationFlowDTO {
	
	public String appNo;
	
	public List<FlowDTO> flow;
	
	public String status;
	
	public String name;
	
	public String designation;
	
	public String office;
	
	public String scaleOfPay;
	
	public String pendingWith;
	
}
