package sad.storereg.dto.appdata;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingAllotmentsDTO {

	public String appNo;
	
	public String name;
	
	public String designation;
	
	public String dept;
	
	public String office;
	
	public String letterNo;
	
	public String memoNo;
	
	public Date orderGeneratedOn;
	
	public Integer csApproved;
	
	public Date csTimestamp;
	
	public Integer chFinalApprove;
	
	public Date chTimestamp;
	
	public Integer applicantAccepted;
	
	public Date applicantTimestamp;
	
	public String pendingWith;
}
