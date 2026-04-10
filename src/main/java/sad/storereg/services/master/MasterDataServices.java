package sad.storereg.services.master;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.UnitRateDTO;
import sad.storereg.dto.master.UnitRequestDTO;
import sad.storereg.exception.InternalServerError;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.Unit;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.RateRepository;
import sad.storereg.repo.master.UnitRepository;
import sad.storereg.repo.master.YearRangeRepository;
import sad.storereg.services.appdata.PurchaseService;

@Service
@RequiredArgsConstructor
public class MasterDataServices {
	
	private final CategoryRepository categoryRepo;
	
	private final UnitRepository unitRepo;
	
	private final RateRepository rateRepository;
	
	private final YearRangeRepository yearRangeRepository;
	
	private final PurchaseService purchaseService;
	
	public List<Category> getCategories(String stockType, Integer officeCode) {
		try {
			if(stockType!=null && stockType.length()==1)
				return categoryRepo.findAllByStockTypeAndOfficeCode(stockType,officeCode);
				
			else
				return categoryRepo.findAllByOfficeCode(officeCode);
				
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<Unit> getUnits(Integer officeCode) {
		try {
				return unitRepo.findAllByOfficeCode(officeCode);
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<UnitRateDTO> getUnitsRates(UnitRequestDTO request, Integer officeCode) {
		try {
			int year = request.getPurchaseDate().getYear();
			YearRange yearRange = yearRangeRepository.findByStartYearLessThanEqualAndEndYearGreaterThanEqualAndOfficeCode(year, year, officeCode).orElseThrow(()->new UnauthorizedException("Rate for year "+year+" has not been defined in master data"));

			List<Rate> rates = rateRepository.findRatesByItemAndOptionalSubItem(request.getItemId(), request.getSubItemId(), yearRange.getId());
			// Map Rate entities to UnitRateDTO
	        return rates.stream()
	                .map(rate -> {
	                    UnitRateDTO dto = new UnitRateDTO();
	                    dto.setUnitId(rate.getUnit().getId());
	                    dto.setUnitName(rate.getUnit().getUnit());
	                    dto.setRate(rate.getRate());
	                    dto.setUnit(rate.getUnit().getName());
	                    return dto;
	                })
	                .toList();
			}catch(Exception ex) {
			throw ex;
		}
    }

	
	public List<UnitRateDTO> getUnitsRatesByDate(LocalDate date, Integer officeCode) {
		try {
			int year = date.getYear();
			YearRange yearRange = yearRangeRepository.findByStartYearLessThanEqualAndEndYearGreaterThanEqualAndOfficeCode(year, year, officeCode).orElseThrow(()->new UnauthorizedException("Rate for year "+year+" has not been defined in master data"));

			List<Rate> rates = rateRepository.findByYearRange_Id(yearRange.getId());
					
			// Map Rate entities to UnitRateDTO
	        return rates.stream()
	                .map(rate -> {
	                    UnitRateDTO dto = new UnitRateDTO();
	                    dto.setUnitId(rate.getUnit().getId());
	                    dto.setUnitName(rate.getUnit().getUnit());
	                    dto.setRate(rate.getRate());
	                    dto.setItemId(rate.getItem().getId());
	                    dto.setSubItemId(rate.getSubItem()!=null?rate.getSubItem().getId():null);
	                    dto.setUnit(rate.getUnit().getName());
	                    //dto.setBalance(purchaseService.getAvailableBalance(rate.getItem().getId(), rate.getSubItem()==null?null:rate.getSubItem().getId(), rate.getUnit().getId(), date));
	                    return dto;
	                })
	                .toList();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<UnitRateDTO> getUnitsBalance(LocalDate issueDate) {
		try {
			List<Rate> rates = rateRepository.findAll();
					
			// Map Rate entities to UnitRateDTO
	        return rates.stream()
	                .map(rate -> {
	                	//System.out.println("Item id : "+rate.getItem().getId());
	                    UnitRateDTO dto = new UnitRateDTO();
	                    //dto.setUnitId(rate.getUnit().getId());
	                    //dto.setUnitName(rate.getUnit().getUnit());
	                    dto.setItemId(rate.getItem().getId());
	                    dto.setSubItemId(rate.getSubItem()!=null?rate.getSubItem().getId():null);
	                    //dto.setUnit(rate.getUnit().getName());
	                    if(rate.getItem().getBaseUnit()!=null) {
		                    dto.setUnitId(rate.getItem().getBaseUnit().getId());
		                    dto.setUnitName(rate.getItem().getBaseUnit().getUnit());
		                    dto.setUnit(rate.getItem().getBaseUnit().getUnit());
	                    }
	                    dto.setBalance(purchaseService.getAvailableBalance(rate.getItem().getId(), rate.getSubItem()==null?null:rate.getSubItem().getId(), rate.getUnit().getId(), issueDate==null?LocalDate.now():null));
	                    return dto;
	                })
	                .toList();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public String createCategory(Category request, Integer officeCode) {
		try {
			if(categoryRepo.findByOfficeCodeAndCodeOrOfficeCodeAndName(officeCode, request.getCode(), officeCode, request.getName()).isPresent())
				throw new UnauthorizedException("Category/Code exists");
	    	Category category  = new Category();
	    	category.setName(request.getName());
	    	category.setCode(request.getCode());
	    	category.setStockType(request.getStockType());
	    	category.setOfficeCode(officeCode);
	    	categoryRepo.save(category);
	    	return "Added successfully";
		}catch(Exception ex) {
			throw ex;
		}
    }
	
	public String updateCategory(Category request, Integer officeCode) {
		try {
			Optional<Category> category=categoryRepo.findByOfficeCodeAndCodeOrOfficeCodeAndName(officeCode, request.getCode(), officeCode, request.getName());
			if(category.isEmpty())
				throw new UnauthorizedException("Category/Code does not exist");
	    	
	    	category.get().setName(request.getName());
	    	categoryRepo.save(category.get());
	    	return "Updated successfully";
		}catch(Exception ex) {
			throw ex;
		}
    }
	
	public String createUnit(Unit request, Integer officeCode) {
		try {
			if(unitRepo.findByOfficeCodeAndUnitOrOfficeCodeAndName(officeCode, request.getUnit(), officeCode, request.getName()).isPresent())
				throw new UnauthorizedException("Unit exists");
	    	Unit unit  = new Unit();
	    	unit.setName(request.getName());
	    	unit.setUnit(request.getUnit());
	    	unit.setOfficeCode(officeCode);
	    	unitRepo.save(unit);
	    	return "Added successfully";
		}catch(Exception ex) {
			throw ex;
		}
    }
	
	public Unit getUnit(Integer unitId) {
		try {
				return unitRepo.findById(unitId).orElseThrow();
			}catch(Exception ex) {
			throw ex;
		}
    }
}
