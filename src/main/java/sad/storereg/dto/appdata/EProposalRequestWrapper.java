package sad.storereg.dto.appdata;

import lombok.Data;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.EProposalRequest;

@Data
public class EProposalRequestWrapper {

	private EProposalRequest request;
    private Applications application;
}
