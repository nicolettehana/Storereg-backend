package sad.storereg.services.appdata;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.nio.file.Paths;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.OccupantDTO;
import sad.storereg.dto.master.QuarterRequestDTO;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.AllotmentsRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.QuartersRepository;

@Service
@RequiredArgsConstructor
public class OccupantServices {
	
	private final QuartersRepository quartersRepo;
	private final AllotmentsRepository allotmentRepo;
	private final ApplicationsRepository appRepo;
	
	@Value("${orders.dir}")
    private String ordersDir;

	@Transactional
	public void addOccupant(QuarterRequestDTO request, Long userCode) {
		
		checkQuarterVacancy(request.getQuarterNo());
		
		//Application
		Applications app = createApplication(request, userCode);
		
		//Allotment
		Allotments allotment = createAllotment(request, app.getAppNo(), userCode);
		
		app.setAllotmentId(allotment.getId());
		appRepo.save(app);
		
		//Quarters
		updateQuarters(request, allotment.getId());
	}
	
//	@Transactional
//	public void addOccupant(OccupantDTO request, Long userCode) {
//		
//		checkQuarterVacancy(request.getQuarterNo());
//		
//		//Application
//		Applications app = createApplication(request, userCode);
//		
//		//Allotment
//		Allotments allotment = createAllotment(request, app.getAppNo(), userCode);
//		
//		app.setAllotmentId(allotment.getId());
//		appRepo.save(app);
//		
//		//Quarters
//		updateQuarters(request, allotment.getId());
//	}
	
	private void checkQuarterVacancy(String quarterNo) {
		Quarters quarter = quartersRepo.findByQuarterNo(quarterNo).orElseThrow();
		if(quarter.getQuarterStatus()==1 || quarter.getQuarterStatus()==2 || quarter.getQuarterStatus()==3 || quarter.getQuarterStatus()==4 || quarter.getInAllotmentList()==1) {
			throw new UnauthorizedException("Quarter not vacant/reserved or in allotment list");
		}
	}
	
	@Transactional
	private Applications createApplication(QuarterRequestDTO request, Long userCode) {
		Applications app = new Applications();
		app.setAppNo(generateApplicationNo());
		app.setName(request.getName());
		app.setDesignation(request.getDesignation());
		app.setGender(request.getGender());
		app.setLevel(-1);
		app.setAppStatus(15);
		//app.setUsername(username);
		app.setUserCode(userCode);;
		app.setEntrydate(LocalDateTime.now());
		app.setDepartmentOrDirectorate(request.getDeptOffice());
		app.setScaleOfPay(request.getPayScale());
		app.setDateOfRetirement(request.getRetirementDate());
		return(appRepo.save(app));
		
	}
	
	@Transactional
	private Allotments createAllotment(QuarterRequestDTO request, String appNo, Long userCode) {
		Allotments allotment = new Allotments();
		allotment.setQuarterNo(request.getQuarterNo());
		allotment.setAppNo(appNo);
		allotment.setEntrydate(new Date());
		//allotment.setUsername(username);
		allotment.setUserCode(userCode);
		allotment.setCsDecisionTimestamp(request.getAllotmentDate()==null?null:Date.from(request.getAllotmentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
		allotment.setOccupationDate(request.getOccupationDate());
		if(appNo.startsWith("Old")) {
			allotment.setFilename(Paths.get(ordersDir, "NoData.pdf").toString());
		}
		return(allotmentRepo.save(allotment));
	}
	
	@Transactional
	private void updateQuarters(QuarterRequestDTO request, Long allotmentId) {
		Quarters quarter = quartersRepo.findByQuarterNo(request.getQuarterNo()).orElseThrow();
		if(request.getQuarterStatus()==1 || request.getQuarterStatus()==2)
			quarter.setQuarterStatus(request.getQuarterStatus());
		quarter.setAllotmentId(allotmentId.intValue());
		quartersRepo.save(quarter);
	}
	
	
	private String generateApplicationNo() {
        String prefix = "Old";
        SecureRandom random = new SecureRandom();

        int randomNumber = 100000 + random.nextInt(900000);

        return prefix + randomNumber;
    }
}
