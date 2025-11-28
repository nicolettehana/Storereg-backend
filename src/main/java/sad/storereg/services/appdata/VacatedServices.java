package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.MyFilter;
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.Vacated;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.AllotmentsRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.appdata.VacatedRepository;
import sad.storereg.repo.master.QuartersRepository;

import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
public class VacatedServices {
	
	private final VacatedRepository vacatedRepo;
	private final QuartersRepository quartersRepo;
	private final ApplicationsRepository appRepo;
	private final AllotmentsRepository allotmentRepo;
	
	@Transactional
	public void markAsVacated(Vacated request, String username, Long allotmentId) {
		try {
			request.setAllotmentId(allotmentId);
			request.setByUser(username);
			request.setEntrydate(new Date());
			vacatedRepo.save(request);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public void createVacated(LocalDate vacateDate, String username, String quarterNo, String appNo, Long allotmentId) {
		try {
			Vacated vacated = new Vacated();
			vacated.setAllotmentId(allotmentId);
			vacated.setAppNo(appNo);
			vacated.setByUser(username);
			vacated.setEntrydate(new Date());
			vacated.setQuarterNo(quarterNo);
			vacated.setVacateDate(vacateDate);
			
			vacatedRepo.save(vacated);
			
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Page<QuartersDTO> getVacated(MyFilter filter, int page, int size){
		try {
			PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "id");
			
			Page<Vacated> pageVacated = vacatedRepo.findByVacateDateBetween(filter.getFromDate(), filter.getToDate(), pageable);
			
			List<QuartersDTO> list = getList(pageVacated.getContent());
			
			return new PageImpl<>(list, pageable, pageVacated.getTotalElements());
		}catch(Exception ex) {
			throw ex;
		}		
	}
	
	private List<QuartersDTO> getList(List<Vacated> list){
		return list.stream().map(vacated -> {
            QuartersDTO dto = new QuartersDTO();
            
            Quarters quarter = quartersRepo.findByQuarterNo(vacated.getQuarterNo()).orElseThrow();
            
			dto.setQuarterNo(vacated.getQuarterNo());
			dto.setQuarterTypeCode(quarter.getQuarterTypeCode());
			dto.setLocation(quarter.getLocation());
			dto.setAppNo(vacated.getAppNo().startsWith("Old")?"-":vacated.getAppNo());
			
			Applications app = appRepo.findByAppNo(vacated.getAppNo()).orElseThrow();
	
			dto.setDateOfRetirement(app.getDateOfRetirement());
	        dto.setDepartment(app.getDepartmentOrDirectorate());
	        dto.setDesignation(app.getDesignation());
	        dto.setName(app.getName());
	        
	        Allotments allotment = allotmentRepo.findById(vacated.getAllotmentId()).orElseThrow();
	        dto.setDateOfOccupation(allotment.getOccupationDate());
	        dto.setPayScale(app.getScaleOfPay());
	        dto.setVacatedDate(allotment.getVacateDate());
            
            return dto;
		 }).collect(Collectors.toList());
	}

}
