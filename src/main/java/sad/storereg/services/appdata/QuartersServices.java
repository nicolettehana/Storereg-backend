package sad.storereg.services.appdata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.appdata.VacateRequest;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.QuartersRepository;

@Service
@RequiredArgsConstructor
public class QuartersServices {
	
	private final QuartersRepository quartersRepo;
	private final AllotmentServices allotmentService;
	private final ApplicationsRepository appRepo;
	
	public Page<QuartersDTO> getAllQuarters(Integer page, Integer size, String role) {
		//PageRequest pageable = PageRequest.of(page, size, Direction.fromString("DESC"), "quarterNo");
		PageRequest pageable = PageRequest.of(page, size);
		Page<Quarters> list = null;
		if(role.equals("EST"))
			list = quartersRepo.findAllByOrderByQuarterNoAsc(pageable);
		else {
			List<String> excludedCodes = Arrays.asList("A", "G");
			list = quartersRepo.findByQuarterTypeCodeNotInAndIsEnabledEqualsOrderByQuarterNoAsc(excludedCodes, 1, pageable);
		}
		return getList(list);		
	}
	
	public Page<QuartersDTO> getOccupiedQuarters(Integer page, Integer size, String status, String role) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<Quarters> list = null;
		Integer statuss=null;
		if(status.equals("all"))
			list=quartersRepo.findAllByQuarterStatusOrQuarterStatusOrderByQuarterNoAsc(1,2, pageable);
		if(status.equalsIgnoreCase("vacant"))
			statuss=0;
		if(status.equalsIgnoreCase("occupied"))
			statuss=1;
		if(status.equalsIgnoreCase("allotted"))
			statuss=2;
		if(status.equalsIgnoreCase("major repair"))
			statuss=3;
		if(status.equalsIgnoreCase("unusable"))
			statuss=4;
		if(status.equalsIgnoreCase("reserved"))
			statuss=5;

		if(role.equals("EST"))
			list = quartersRepo.findAllByQuarterStatusOrderByQuarterNoAsc(statuss, pageable);
		else//Here remove disbaled quarters for GAD role
			list = quartersRepo.findAllByQuarterStatusAndIsEnabledEqualsOrderByQuarterNoAsc(statuss, 1, pageable);
		
//		// Separate valid and invalid quarterNos
//		List<Quarters> valid = list.stream()
//		    .filter(q -> q.getQuarterNo() != null && q.getQuarterNo().split("-").length == 3)
//		    .collect(Collectors.toList());
//
//		List<Quarters> invalid = list.stream()
//		    .filter(q -> q.getQuarterNo() == null || q.getQuarterNo().split("-").length != 3)
//		    .collect(Collectors.toList());
//
//		// Sort only the valid ones
//		valid.sort(Comparator
//		    .comparing((Quarters q) -> q.getQuarterNo().split("-")[0]) // First part
//		    .thenComparing(q -> Integer.parseInt(q.getQuarterNo().split("-")[1])) // Second part
//		    .thenComparing(q -> q.getQuarterNo().split("-")[2])); // Third part
//
//		// Combine sorted valid and untouched invalid
//		valid.addAll(invalid);
		
		return getList(list);
	}
	
	public Page<QuartersDTO> getOccupiedQuartersWithType(Integer page, Integer size, String status, String type, String role) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<Quarters> list = null;
		if(role.equals("EST")) {
			if(status.equals("all"))
				list=quartersRepo.findAllByQuarterStatusOrQuarterStatusOrderByQuarterNoAsc(1,2,pageable);
			else if(status.equals("occupied")) {
				
				list = quartersRepo.findAllByQuarterStatusAndQuarterTypeCodeEqualsOrderByQuarterNoAsc(1,type, pageable);
			}
			else {
				list = quartersRepo.findAllByQuarterStatusAndQuarterTypeCodeEqualsOrderByQuarterNoAsc(2,type, pageable);
			}
		}
		else {
			if(status.equals("all")) // you need to fix this one imp
				list=quartersRepo.findAllByQuarterStatusOrQuarterStatusOrderByQuarterNoAsc(1,2,pageable);
			else if(status.equals("occupied")) {
				
				list = quartersRepo.findAllByQuarterStatusAndQuarterTypeCodeEqualsAndIsEnabledEqualsOrderByQuarterNoAsc(1,type,1, pageable);
			}
			else {
				list = quartersRepo.findAllByQuarterStatusAndQuarterTypeCodeEqualsAndIsEnabledEqualsOrderByQuarterNoAsc(2,type,1, pageable);
			}
		}
		
//		// Separate valid and invalid quarterNos
//		List<Quarters> valid = list.stream()
//		    .filter(q -> q.getQuarterNo() != null && q.getQuarterNo().split("-").length == 3)
//		    .collect(Collectors.toList());
//
//		List<Quarters> invalid = list.stream()
//		    .filter(q -> q.getQuarterNo() == null || q.getQuarterNo().split("-").length != 3)
//		    .collect(Collectors.toList());
//
//		// Sort only the valid ones
//		valid.sort(Comparator
//		    .comparing((Quarters q) -> q.getQuarterNo().split("-")[0]) // First part
//		    .thenComparing(q -> Integer.parseInt(q.getQuarterNo().split("-")[1])) // Second part
//		    .thenComparing(q -> q.getQuarterNo().split("-")[2])); // Third part
//
//		// Combine sorted valid and untouched invalid
//		valid.addAll(invalid);
		
		return getList(list);
	}
	
	public Page<QuartersDTO> getFilteredQuarters(Integer page, Integer size, String search, String status, String role){
		PageRequest pageable = PageRequest.of(page, size);
		Page<Quarters> list = null;
		if(role.equals("EST")) {
			if(status==null || status.equals("all"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCase(search, pageable);
			else if(status.equals("occupied"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCaseAndQuarterStatus(search, 1, pageable);
			else if(status.equals("allotted"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCaseAndQuarterStatus(search, 2, pageable);
		}
		else {
			if(status==null || status.equals("all"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCaseAndIsEnabledEquals(search, 1, pageable);
			else if(status.equals("occupied"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCaseAndQuarterStatusAndIsEnabledEquals(search, 1, 1, pageable);
			else if(status.equals("allotted"))
				list = quartersRepo.findByQuarterNoContainingIgnoreCaseAndQuarterStatusAndIsEnabledEquals(search, 2, 1, pageable);
		}
		return getList(list);
	}
	
	public Page<QuartersDTO> getQuartersByQuarterType(Integer page, Integer size, String quarterTypeCode, String role){
		PageRequest pageable = PageRequest.of(page, size);
		Page<Quarters> list = null;
		if(role.equals("EST"))
			list = quartersRepo.findAllByQuarterTypeCode(quarterTypeCode, pageable);
		else
			list = quartersRepo.findAllByQuarterTypeCodeAndIsEnabledEquals(quarterTypeCode, 1, pageable);
			
		return getList(list);
	}
	
	private Page<QuartersDTO> getList(Page<Quarters> list){
		return list.map(quarter -> {
	        QuartersDTO dto = new QuartersDTO();
	        dto.setQuarterNo(quarter.getQuarterNo());
	        dto.setQuarterName(quarter.getQuarterName());
	        dto.setQuarterTypeCode(quarter.getQuarterTypeCode());
	        dto.setLocation(quarter.getLocation());
	        String stat="";
	        if(quarter.getPhysicalStatus()==2)
	        	stat = "Minor Repair - ";
	        dto.setStatus(
	            quarter.getQuarterStatus() == 1 ? stat+"Occupied" :
	            quarter.getQuarterStatus() == 2 ? stat+"Allotted but not yet occupied" :
	            quarter.getQuarterStatus() == 3 ? stat+"Major Repair" :
	            quarter.getQuarterStatus() == 4 ? stat+"Unusable" :
	            quarter.getQuarterStatus() == 5 ? stat+"Reserved" :
	            stat+"Vacant"
	        );
	        dto.setIsEnabled(quarter.getIsEnabled());

	        if (quarter.getQuarterStatus() == 0) {
	            dto.setDateOfRetirement(null);
	            dto.setDepartment(null);
	            dto.setDesignation(null);
	            dto.setName(null);
	            dto.setDateOfOccupation(null);
	        } else {
	            Map<String, String> app = getApplicantData(quarter.getQuarterNo(), quarter.getAllotmentId());
	            dto.setDateOfRetirement((app.get("dateOfRetirement") == null || app.get("dateOfRetirement").equals("null")) ? null : LocalDate.parse(app.get("dateOfRetirement")));
	            dto.setDepartment(app.get("department"));
	            dto.setDesignation(app.get("designation"));
	            dto.setName(app.get("name"));
	            dto.setDateOfOccupation((app.get("dateOfOccupation") == null || app.get("dateOfOccupation").equals("null")) ? null : LocalDate.parse(app.get("dateOfOccupation")));
	            dto.setPayScale(app.get("payScale"));
	            dto.setAppNo(app.get("appNo"));
	            dto.setDateOfAllotment(app.get("dateOfAllotment") == null ? null : LocalDate.parse(app.get("dateOfAllotment")));
	            dto.setApplicantLetterCode(app.get("applicantLetterCode"));
	        }

	        return dto;
	    });
	}
	
	private Map<String, String> getApplicantData(String quarterNo, Integer allotmentId2) {
		try {
			Map<String, String> responseMap = new HashMap<>();
			Optional<Allotments> allotment = null;
			if(allotmentId2!=null)
				allotment = allotmentService.getAllotmentById(allotmentId2.longValue());
			//Optional<Allotments> allotment = allotmentService.getAllotment(quarterNo);
			if(allotment!=null && allotment.isPresent()) {
				Optional<Applications> application = appRepo.findByAppNo(allotment.get().getAppNo());
				
				responseMap.put("name", application.isPresent()?application.get().getName():"-");
				responseMap.put("designation", application.isPresent()?application.get().getDesignation():"-");
				responseMap.put("department", application.isPresent()?application.get().getDepartmentOrDirectorate():"-");
				responseMap.put("dateOfRetirement", application.isPresent()?application.get().getDateOfRetirement()+"":null);
				responseMap.put("payScale", application.isPresent()?application.get().getScaleOfPay()+"":null);
				responseMap.put("appNo", application.isPresent()?application.get().getAppNo()+"":null);
				
				String dateOfOccupation = null;
				String dateOfAllotment = null;
				if (application.isPresent()) {
				    Long allotmentId = application.get().getAllotmentId();
				    if (allotmentId != null) {
				        Optional<Allotments> all = allotmentService.getAllotmentById(allotmentId);
				        if (all.isPresent()) {
				            dateOfOccupation = all.get().getOccupationDate() + "";
				            dateOfAllotment = all.get().getCsDecisionTimestamp()==null?null:all.get().getCsDecisionTimestamp().toInstant()
	                                  .atZone(ZoneId.systemDefault())
	                                  .toLocalDate()+"";
				        }
				    }
				}				
				responseMap.put("dateOfOccupation", dateOfOccupation);
				responseMap.put("dateOfAllotment", dateOfAllotment);
				responseMap.put("applicantLetterCode", allotment.get().getApplicantLetter());
			}
			else {
				responseMap.put("name", "-");
				responseMap.put("designation", "-");
				responseMap.put("department", "-");
				responseMap.put("dateOfRetirement",null);
				responseMap.put("dateOfOccupation",null);
				responseMap.put("payScale", null);
				responseMap.put("appNo", null);
				responseMap.put("applicantLetterCode", null);
			}
			return responseMap;
		}catch(Exception ex) {
			throw ex;
		}
	}	

	public List<QuartersDTO> getQuartersList(User user) {
        try {        	
        	List<Quarters> list = quartersRepo.findAvailableQuarters(user.getId());
            
        	return list.stream()
                    .map(q -> new QuartersDTO(q.getQuarterNo(), q.getQuarterName(),null,null,null,null,null,null,null,null,null,null,null,null,
                    		null,null))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new InternalServerError("Unable to fetch vacateRequests", e);
        }
    }
	
	public Quarters getQuarter(String quarterNo) {
		try {
			return quartersRepo.findByQuarterNo(quarterNo).orElseThrow(()-> new ObjectNotFoundException("Quarter not found"));
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public Allotments getAllotment(String quarterNo) {
		try {
			Quarters quarter = quartersRepo.findByQuarterNo(quarterNo).orElseThrow(()-> new ObjectNotFoundException("Quarter not found"));
			return allotmentService.getAllotmentById(quarter.getAllotmentId().longValue()).get();
		}catch(Exception ex) {
			throw ex;
		}
	}
}
