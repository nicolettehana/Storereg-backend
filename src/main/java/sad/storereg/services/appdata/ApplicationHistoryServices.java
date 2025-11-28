package sad.storereg.services.appdata;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.ApplicationFlowDTO;
import sad.storereg.dto.appdata.FlowDTO;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.ApplicationsHistory;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.ApplicationFlow;
import sad.storereg.repo.appdata.ApplicationHistoryRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.ApplicationFlowRepository;

@Service
@RequiredArgsConstructor
public class ApplicationHistoryServices {
	
	private final ApplicationHistoryRepository appHistoryRepo;
	private final ApplicationsRepository appRepo;
	private final CoreServices coreService;
	private final ApplicationFlowRepository appFlowRepo;
	
	@Transactional
	public ApplicationsHistory save(String appNo, String remarks, Date date, Long userCode, Integer flowCode) {
		try {
			ApplicationsHistory appHistory = ApplicationsHistory.builder().appNo(appNo).remarks(remarks).entrydate(date).userCode(userCode)
					.flowCode(flowCode).build();
			return appHistoryRepo.save(appHistory);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public String getRemarksByCH(String applicationNo) {
		try {
			Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(applicationNo, 5);
			return appHistory.isEmpty()?"-":appHistory.get().getRemarks();
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public String getRemarksByCS(String applicationNo) {
		try {
			Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(applicationNo, 17);
			return appHistory.isEmpty()?"-":appHistory.get().getRemarks();
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public String getApprovalRemarksByCS(String applicationNo) {
		try {
			Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(applicationNo, 7);
			return appHistory.isEmpty()?"-":appHistory.get().getRemarks();
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public String getRemarksByUser(String applicationNo) {
		try {
			Optional<ApplicationsHistory> appHistory = appHistoryRepo.findTopByAppNoAndFlowCodeEqualsOrderByEntrydateDesc(applicationNo, 16);
			return appHistory.isEmpty()?"-":appHistory.get().getRemarks();
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public ApplicationFlowDTO getApplicationHistory(String applicationNo) {
		try {
			
			ApplicationFlowDTO history = new ApplicationFlowDTO();
			Applications app = appRepo.findByAppNo(applicationNo).orElseThrow(()->new ObjectNotFoundException("Invalid application no."));
			
			history.setAppNo(applicationNo);
			history.setName(app.getName());
			history.setDesignation(app.getDesignation());
			history.setOffice(app.getDepartmentOrDirectorate()+", "+app.getOfficeAddress());
			history.setScaleOfPay(app.getScaleOfPay());
			history.setStatus(coreService.getStatus(app.getAppStatus()));
			history.setPendingWith(getUser(app.getLevel()));
			
			history.setFlow(getFlow(applicationNo));
			
			return history;
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	private String getUser(int level) {
	    return switch (level) {
	        case 0 -> "Applicant";
	        case 1 -> "Dealing Asst.";
	        case 2 -> "Chairman";
	        case 3 -> "Chairman";
	        case 4 -> "Chief Secretary";
	        case 5 -> "Estate Officer";
	        default -> "-";
	    };
	}
	
	private List<FlowDTO> getFlow(String appNo){
		List<FlowDTO> flows = new ArrayList<>();
		
		List<ApplicationsHistory> list = appHistoryRepo.findAllByAppNoOrderByEntrydateAsc(appNo);
		for(ApplicationsHistory item: list) {
			FlowDTO flow = new FlowDTO();
			
			ApplicationFlow appFlow = appFlowRepo.findByCode(item.getFlowCode());
			
			flow.setAction(coreService.getAction(appFlow.getToStatus()));
			flow.setRemarks(item.getRemarks());
			flow.setRole(item.getUserCode()==null?null:coreService.getRoleName(item.getUserCode()));
			flow.setTimstamp(item.getEntrydate());
			flow.setUserCode(item.getUserCode()==null?null:item.getUserCode());
			flows.add(flow);
		}
		return flows;
	}
	
	public List<FlowDTO> getOutbox(User user){
		try {
			List<ApplicationsHistory> list = appHistoryRepo.findAllByUserCodeOrderByEntrydateDesc(user.getId());
			List<FlowDTO> flows = new ArrayList<>();
			for(ApplicationsHistory item: list) {
				FlowDTO flow = new FlowDTO();
				
				ApplicationFlow appFlow = appFlowRepo.findByCode(item.getFlowCode());
				
				flow.setAppNo(item.getAppNo());
				flow.setAction(coreService.getAction(appFlow.getToStatus()));
				flow.setRemarks(item.getRemarks());
				flow.setRole(coreService.getRoleName(item.getUserCode()));
				flow.setTimstamp(item.getEntrydate());
				flow.setUserCode(item.getUserCode());
				flows.add(flow);
			}
			return flows;
		}
		catch(Exception ex) {
			throw ex;
		}
	}

}
