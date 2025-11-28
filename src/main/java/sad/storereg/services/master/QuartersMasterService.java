package sad.storereg.services.master;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.QuarterStatsDTO;
import sad.storereg.dto.master.QuarterAssetDTO;
import sad.storereg.dto.master.QuarterRequestDTO;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.master.QuarterDetails;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.master.QuarterDetailsRepository;
import sad.storereg.repo.master.QuartersRepository;
import sad.storereg.services.appdata.CoreServices;

@Service
@RequiredArgsConstructor
public class QuartersMasterService {
	
	private final QuartersRepository quartersRepo;
	
	private final QuarterDetailsRepository quarterDetailsRepo;
	
	private final CoreServices coreServices;
	
	@Transactional
	public void addQuarter(QuarterRequestDTO dto) {
		try {
			
			Quarters quarter = quartersRepo.findById(dto.getQuarterNo())
		            .orElse(new Quarters());
			if(dto.getQuarterStatus()!=null && quarter.getQuarterStatus()!=null && (quarter.getQuarterStatus()==1 || quarter.getQuarterStatus()==2) 
					&& ((dto.getQuarterStatus()!=1 && dto.getQuarterStatus()!=2) || (dto.getPhysicalStatus()!=1))
					)
				throw new UnauthorizedException("Action not allowed. Vacate Quarter First");

			if(dto.getQuarterStatus()==null && (quarter.getQuarterStatus()==1 || quarter.getQuarterStatus()==2) 
					&& dto.getPhysicalStatus()!=1)
				throw new UnauthorizedException("Action not allowed. Vacate Quarter First");
			
			if(dto.getQuarterStatus()!=null && quarter.getQuarterStatus()!=null && quarter.getQuarterStatus()!=1 && (dto.getQuarterStatus()==1 || dto.getQuarterStatus()==2)) {
				if(dto.getName()==null || dto.getDesignation()==null || dto.getGender()==null)
					throw new UnauthorizedException("Enter Occupant Details");
			}
			
			if(dto.getPhysicalStatus()==3 || dto.getPhysicalStatus()==4)
				dto.setQuarterStatus(dto.getPhysicalStatus());
			
			quarter.setQuarterNo(dto.getQuarterNo());
		    quarter.setQuarterName(dto.getQuarterName());
		    quarter.setQuarterTypeCode(dto.getQuarterTypeCode());
		    quarter.setLocation(dto.getLocation());
		    quarter.setPhysicalStatus(dto.getPhysicalStatus());
		    
			if(dto.getQuarterStatus()!=5)
				quarter.setIsEnabled(1);
			else
				quarter.setIsEnabled(0);
			if(dto.getPhysicalStatus()==4 || dto.getQuarterStatus()==3)
		    	quarter.setQuarterStatus(dto.getPhysicalStatus());
		    else
		    	quarter.setQuarterStatus(dto.getQuarterStatus());
			//quarter.setInAllotmentList(0);
			quarter.setInAllotmentList(Optional.ofNullable(dto.getInAllotmentList()).orElse(0));
		    quarter.setIsEnabled(dto.getQuarterStatus() != null && dto.getQuarterStatus() != 5 ? 1 : 0);

		    quarter.setAllotmentId(dto.getAllotmentId());
		    quarter.setDistrictCode(dto.getDistrictCode());
		    quarter.setBlockCode(dto.getBlockCode());
		    quarter.setVillageCode(dto.getVillageCode());
		    
		    quarter.setDepartmentCode(dto.getDepartmentCode());
		    quarter.setOfficeCode(dto.getOfficeCode());
			quartersRepo.save(quarter);
			
			saveQuarterDetails(dto);
			
		}catch(Exception ex){
			throw ex;
		}
	}
	
	@Transactional
	public void saveOrUpdateQuarterWithDetails(QuarterRequestDTO dto) {
		Quarters quarter = quartersRepo.findById(dto.getQuarterNo())
	            .orElse(new Quarters());
	    quarter.setQuarterNo(dto.getQuarterNo());
	    quarter.setQuarterName(dto.getQuarterName());
	    quarter.setQuarterTypeCode(dto.getQuarterTypeCode());
	    quarter.setLocation(dto.getLocation());
	    quarter.setPhysicalStatus(dto.getPhysicalStatus());
	    
	    if(quarter.getPhysicalStatus()==3 || (quarter.getPhysicalStatus()==4))
	    	quarter.setQuarterStatus(quarter.getPhysicalStatus());
	    else
	    	quarter.setQuarterStatus(0);
	    
		quarter.setInAllotmentList(0);
	    
	    //if(dto.getPhysicalStatus()==4 || dto.getQuarterStatus()==3)
	    //	quarter.setQuarterStatus(dto.getPhysicalStatus());
	    //else
	    //	quarter.setQuarterStatus(dto.getQuarterStatus());
	    
	    quarter.setInAllotmentList(Optional.ofNullable(dto.getInAllotmentList()).orElse(0));
	    quarter.setIsEnabled(dto.getQuarterStatus() != null && dto.getQuarterStatus() != 5 ? 1 : 0);

	    quarter.setAllotmentId(dto.getAllotmentId());
	    quarter.setDistrictCode(dto.getDistrictCode());
	    quarter.setBlockCode(dto.getBlockCode());
	    quarter.setVillageCode(dto.getVillageCode());
	    
	    quarter.setDepartmentCode(dto.getDepartmentCode());
	    quarter.setOfficeCode(dto.getOfficeCode());

	    quartersRepo.save(quarter);

	    saveQuarterDetails(dto);
	}

	
	public List<QuarterStatsDTO> getQuarterStats(String role) {
		List<String> quarterTypes = null;
		if(role.equals("EST"))
			quarterTypes = List.of("A","B", "C", "D", "E", "G");
		else
			quarterTypes = List.of("B", "C", "D", "E");
	    List<QuarterStatsDTO> stats = new ArrayList<>();
	    
	    int totalOccupied=0;
	    int totalVacant=0;
	    int totalOverall=0;
	    int totalReserved=0;
	    int totalCondemned=0;
	    int totalUnderMaintenance=0;

	    for (String type : quarterTypes) {
	        List<Object[]> rows = quartersRepo.getQuarterStatsByType(type);
	        if (!rows.isEmpty()) {
	            Object[] row = rows.get(0);
	            String quarterTypeCode = (String) row[0];
	            int occupied = ((Number) row[1]).intValue();
	            int vacant = ((Number) row[2]).intValue();
	            int total = ((Number) row[3]).intValue();
	            int reserved = ((Number) row[4]).intValue();
	            int condemned = ((Number)row[5]).intValue();
	            int underMaintenance = ((Number)row[6]).intValue();
	            
	            totalOccupied=totalOccupied+occupied;
	            totalVacant=totalVacant+vacant;
	            totalOverall=totalOverall+total;
	            totalReserved=totalReserved+reserved;
	            totalCondemned=totalCondemned+condemned;
	            totalUnderMaintenance=totalUnderMaintenance+underMaintenance;

	            stats.add(new QuarterStatsDTO(quarterTypeCode, occupied, vacant, total,reserved, 0,0));
	        } else {
	            // In case that type has no data in DB, add 0s
	            stats.add(new QuarterStatsDTO(type, 0, 0, 0, 0, 0, 0));
	        }
	    }
	    stats.add(new QuarterStatsDTO("total", totalOccupied, totalVacant, totalOverall,totalReserved,totalCondemned, totalUnderMaintenance));

	    return stats;
	}
	
	public List<QuarterAssetDTO> getAllQuarterAssetInfo() {
	    List<Quarters> quartersList = quartersRepo.findAll();

	    List<QuarterAssetDTO> dtos = new ArrayList<>();
	    for (Quarters quarter : quartersList) {
	        QuarterDetails details = quarterDetailsRepo.findById(quarter.getQuarterNo()).orElse(null);

	        QuarterAssetDTO dto = new QuarterAssetDTO();
	        if (quarter.getDepartmentCode() != null) {
	            dto.setDepartmentName(coreServices.getDepartment(quarter.getDepartmentCode()).getDeptName());
	        }

	        if (quarter.getOfficeCode() != null) {
	            dto.setOfficeName(coreServices.getOffice(quarter.getOfficeCode()).getOfficeName());
	        }


	        if (details != null) {
	            dto.setAssetType(details.getAssetType());
	            dto.setAssetDescription(details.getAssetDescription());
	            dto.setLatitude(details.getLatitude());
	            dto.setLongitude(details.getLongitude());
	            dto.setLandAssetId(details.getLandAssetId());
	            dto.setAssetCategory(details.getAssetCategory());
	            dto.setStructureType(details.getStructureType());
	            dto.setInaugurationDate(details.getInaugurationDate());
	            dto.setBuiltUpArea(details.getBuiltUpArea());
	            dto.setManagedBy(details.getManagedBy());
	        }

	        if (quarter.getDistrictCode() != null) {
	            dto.setDistrict(coreServices.getDistrict(quarter.getDistrictCode()).getDistrictName());
	        }

	        if (quarter.getBlockCode() != null) {
	            dto.setBlock(coreServices.getBlock(quarter.getBlockCode()).getBlockName());
	        }

	        if (quarter.getVillageCode() != null) {
	            dto.setVillage(coreServices.getVillage(quarter.getVillageCode()).getVillageName());
	        }

	        dto.setLocation(quarter.getLocation());

	        if(quarter.getPhysicalStatus() != null)
	        	dto.setPresentCondition(coreServices.getPhysicalStatus(quarter.getPhysicalStatus().shortValue()).getPhysicalStatus());

	        dtos.add(dto);
	    }

	    return dtos;
	}

	private QuarterDetails saveQuarterDetails(QuarterRequestDTO dto) {
		QuarterDetails details = quarterDetailsRepo.findById(dto.getQuarterNo()).orElse(new QuarterDetails());
	    details.setQuarterNo(dto.getQuarterNo());
	    details.setInaugurationDate(dto.getInaugurationDate());
	    details.setAssetType(dto.getAssetType());
	    details.setAssetDescription(dto.getAssetDescription());
	    details.setLatitude(dto.getLatitude());
	    details.setLongitude(dto.getLongitude());
	    details.setLandAssetId(dto.getLandAssetId());
	    details.setAssetCategory(dto.getAssetCategory());
	    details.setStructureType(dto.getStructureType());
	    details.setBuiltUpArea(dto.getBuiltUpArea());
	    details.setManagedBy(dto.getManagedBy());

	    return quarterDetailsRepo.save(details);
	}
	
//	public List<QuarterStatsDTO> getQuarterStats2() {
//		List<QuarterStatsDTO> list;
//		List<String> quarterTypes = List.of("B", "C", "D", "E", "G");  // Add all types as needed
//	    List<QuarterStatsDTO> stats = new ArrayList<>();
//	    
//	    // Fetch and build stats one by one
//	    for (String type : quarterTypes) {
//	        List<Object[]> row = quartersRepository.getQuarterStatsByType(type);
//	        String quarterTypeCode = (String) row[0];
//	        int occupied = ((Number) row[1]).intValue();
//	        int vacant = ((Number) row[2]).intValue();
//	        int total = ((Number) row[3]).intValue();
//
//	        // Add to the stats list
//	        stats.add(new QuarterStatsDTO(quarterTypeCode, occupied, vacant, total));
//	    }
//	    System.out.println("Hey: "+stats);
////        List<QuarterStatsDTO> list = quartersRepository.getQuarterStatisticsByType();
////
////        long totalOccupied = 0, totalVacant = 0, totalCount = 0;
////        for (QuarterStatsDTO dto : list) {
////            totalOccupied += dto.getOccupied();
////            totalVacant += dto.getVacant();
////            totalCount += dto.getTotal();
////        }
////
////        list.add(new QuarterStatsDTO("TOTAL", totalOccupied, totalVacant, totalCount));
////
////        return list;
//        return stats;
//    }

}
