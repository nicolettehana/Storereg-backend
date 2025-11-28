package sad.storereg.controller.master;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sad.storereg.annotations.Auditable;
import sad.storereg.dto.appdata.QuarterStatsDTO;
import sad.storereg.dto.appdata.QuartersDTO;
import sad.storereg.dto.master.QuarterAssetDTO;
import sad.storereg.dto.master.QuarterRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.Allotments;
import sad.storereg.models.appdata.Applications;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Quarters;
import sad.storereg.repo.appdata.AllotmentsRepository;
import sad.storereg.repo.appdata.ApplicationsRepository;
import sad.storereg.repo.master.QuartersRepository;
import sad.storereg.services.appdata.QuartersServices;
import sad.storereg.services.master.QuartersMasterService;

@RestController
@RequiredArgsConstructor
public class QuartersController {
	
	private final QuartersRepository quartersRepo;
	private final QuartersServices quartersService;
	private final QuartersMasterService quartersMasterService;
	private final ApplicationsRepository appRepo;
	private final AllotmentsRepository allotmentRepo;
	
	@GetMapping("/quarters/vacant/{quarterType}")
	public List<Quarters> getVacantQuarters(@PathVariable String quarterType) throws IOException {
		try {
			//return quartersRepo.findAllByQuarterTypeCodeAndIsOccupiedEquals(quarterType, 0);
			//return quartersRepo.findAllByQuarterTypeCodeAndInAllotmentListAndIsEnabledEquals(quarterType, 0, 1);
			return quartersRepo.findAllByQuarterTypeCodeAndInAllotmentListAndQuarterStatusAndIsEnabledEquals(quarterType, 0, 0, 1);
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarters", ex);
		}
	}
	
	@GetMapping("/quarters/vacant-reserved")
	public List<Quarters> getVacantReserved() throws IOException {
		try {
			//return quartersRepo.findAllByQuarterTypeCodeAndIsOccupiedEquals(quarterType, 0);
			return quartersRepo.findAllByQuarterStatusInAndInAllotmentListOrderByQuarterNoAsc(Arrays.asList(0, 5), 0);

		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarters", ex);
		}
	}
	
	@GetMapping("/quarters/stats")
	public List<QuarterStatsDTO> getQuartersStats(@AuthenticationPrincipal User user) throws IOException {
		try {
			return quartersMasterService.getQuarterStats(user.getRole().name());
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarters", ex);
		}
	}
	
	@GetMapping(value="/quarters/{quarterType}",params = { "page", "size" })
	public Page<QuartersDTO> getQuartersByType(@PathVariable String quarterType,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam("page") final int page,
            @RequestParam("size") final int size, @AuthenticationPrincipal User user) throws IOException {
		try {
			if(search!=null && search.length()!=0) 
				return quartersService.getFilteredQuarters(page, size, search, null, user.getRole().name());
			
			return quartersService.getQuartersByQuarterType(page, size, quarterType, user.getRole().name());
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarters", ex);
		}
	}
	
	@GetMapping("/quarters")
	public Page<QuartersDTO> getQuarters(@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
	        @RequestParam(value = "size", defaultValue = "10") Integer size, @AuthenticationPrincipal User user) throws IOException {
		try {
			
			if(search!=null && search.length()>0)
				return quartersService.getFilteredQuarters(page, size, search, status, user.getRole().name());
			if(status == null || status.equals("all"))
				return quartersService.getAllQuarters(page, size, user.getRole().name());
			else if (status.equalsIgnoreCase("occupied") || status.equalsIgnoreCase("allotted") || status.equalsIgnoreCase("Major repair")
					|| status.equalsIgnoreCase("unusable") || status.equalsIgnoreCase("reserved") || status.equalsIgnoreCase("vacant")) {
				if(type == null || type.length()==0)
	            	return quartersService.getOccupiedQuarters(page, size, status, user.getRole().name());
	            else{
	            	return quartersService.getOccupiedQuartersWithType(page, size, status, type, user.getRole().name());
	            }
	        } else {
	            throw new IllegalArgumentException("Invalid status parameter");
	        }
		} catch (UnauthorizedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalServerError("Unable to fetch quarters", ex);
		}
	}
	
	@Auditable
	@PostMapping("/quarters")
	public ResponseEntity<Map<String, String>> addQuarter(@Valid @RequestBody QuarterRequestDTO request,
			@AuthenticationPrincipal User user){
		try {
			Map<String, String> map = new HashMap<>();
			quartersMasterService.addQuarter(request);
			//quartersMasterService.saveOrUpdateQuarterWithDetails(request);
			map.put("detail", "Quarter added successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
			
		}catch (ObjectNotFoundException | UnauthorizedException ex) {
			throw ex;
		}catch(Exception ex) {
			throw new InternalServerError("Unable to add quarter",ex);
		}
	}

	@Auditable
	@PutMapping("/quarters/{quarterNo}")
	public ResponseEntity<Map<String, String>> updateQuarter(@PathVariable String quarterNo, 
	        @Valid @RequestBody QuarterRequestDTO request) {
	    try {
	        Quarters quarter = quartersRepo.findByQuarterNo(quarterNo).orElseThrow(()->new ObjectNotFoundException("Quarter not found"));
	        quartersMasterService.addQuarter(request);
//	        if (request.getLocation() != null)
//	            quarter.setLocation(request.getLocation());
//
//	        if (request.getQuarterTypeCode() != null) 
//	            quarter.setQuarterTypeCode(request.getQuarterTypeCode());
//	        
	        quartersRepo.save(quarter);

	        Map<String, String> map = new HashMap<>();
	        map.put("detail", "Quarter updated successfully");
	        return new ResponseEntity<>(map, HttpStatus.OK);

	    } catch (ObjectNotFoundException | UnauthorizedException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerError("Unable to update quarter", ex);
	    }
	}
	
	@Auditable
	@PutMapping("/quarters/enable-disable/{quarterNo}")
	public ResponseEntity<Map<String, String>> enableDisableQuarter(@PathVariable String quarterNo, @RequestBody Map<String, Integer> requestBody
	        ) {
	    try {
	        Quarters quarter = quartersRepo.findByQuarterNo(quarterNo).orElseThrow(() -> new ObjectNotFoundException("Quarter not found"));

	        Map<String, String> map = new HashMap<>();
	        if(quarter.getQuarterStatus()==1 || quarter.getQuarterStatus()==2) {
	        	map.put("detail", "Quarter is occupied, cannot perform action");
	        	return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
	        }	        	
	        
	        // Extract status (Optional)
	        Integer status;
	        
	        if(quarter.getIsEnabled()==1 ) {
	        	if(requestBody.get("status")==null) {
	        		map.put("detail", "Status is required");
	        		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	        	}
	        	else
	        	{
	        		status = requestBody.get("status");
	        		quarter.setQuarterStatus(status);
	        	}
	        }	 
	        else {
	        	quarter.setQuarterStatus(0);
	        }
	        
	        quarter.setIsEnabled(quarter.getIsEnabled() == 1 ? 0 : 1);
	        
	        map.put("detail", quarter.getIsEnabled() == 0 ? "Quarter disabled successfully" : "Quarter enabled successfully");

	        quartersRepo.save(quarter);
	       
	        return new ResponseEntity<>(map, HttpStatus.OK);

	    } catch (ObjectNotFoundException | UnauthorizedException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerError("Unable to update quarter status", ex);
	    }
	}

	@GetMapping("/api/asset-info")
	public ResponseEntity<List<QuarterAssetDTO>> getAllQuarterAssetInfo() {
	    try {
	        List<QuarterAssetDTO> result = quartersMasterService.getAllQuarterAssetInfo();
	        return new ResponseEntity<>(result, HttpStatus.OK);
	    } catch (Exception ex) {
	        throw new InternalServerError("Failed to retrieve quarter asset info", ex);
	    }
	}
	
	@GetMapping("/quarters/full-details/{quarterNo}")
    public ResponseEntity<QuarterRequestDTO> getQuarterDetails(@PathVariable String quarterNo) {
        QuarterRequestDTO dto = quartersRepo.findQuarterFullDetailsByQuarterNo(quarterNo);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        if(dto.getQuarterStatus()==1)//is occupied
        {
        	Quarters quarter = quartersRepo.findByQuarterNo(dto.getQuarterNo()).orElse(null);
        	Applications app = appRepo.findByAllotmentId(quarter.getAllotmentId().longValue()).orElse(null);
        	if(app!=null) {
        	Allotments allotment = allotmentRepo.findById(app.getAllotmentId()).orElse(null);
        	dto.setName(app.getName());
        	dto.setGender(app.getGender());
        	dto.setAllotmentDate(allotment.getCsDecisionTimestamp()!=null? allotment.getCsDecisionTimestamp().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate():null);
        	dto.setDeptOffice(app.getDepartmentOrDirectorate());
        	dto.setPayScale(app.getScaleOfPay());
        	dto.setRetirementDate(app.getDateOfRetirement());
        	dto.setOccupationDate(allotment.getOccupationDate());
        	dto.setDesignation(app.getDesignation());
        	}
        }
        return ResponseEntity.ok(dto);
    }

}
