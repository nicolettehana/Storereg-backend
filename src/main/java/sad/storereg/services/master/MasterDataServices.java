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
	
	public List<Category> getCategories() {
		try {
				return categoryRepo.findAll();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<Unit> getUnits() {
		try {
				return unitRepo.findAll();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public List<UnitRateDTO> getUnitsRates(UnitRequestDTO request) {
		try {
			int year = request.getPurchaseDate().getYear();
			YearRange yearRange = yearRangeRepository.findByStartYearLessThanEqualAndEndYearGreaterThanEqual(year, year).orElseThrow(()->new UnauthorizedException("Rate for year "+year+" has not been defined in master data"));

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

	
	public List<UnitRateDTO> getUnitsRatesByDate(LocalDate date) {
		try {
			int year = date.getYear();
			YearRange yearRange = yearRangeRepository.findByStartYearLessThanEqualAndEndYearGreaterThanEqual(year, year).orElseThrow(()->new UnauthorizedException("Rate for year "+year+" has not been defined in master data"));

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
	                    UnitRateDTO dto = new UnitRateDTO();
	                    dto.setUnitId(rate.getUnit().getId());
	                    dto.setUnitName(rate.getUnit().getUnit());
	                    dto.setItemId(rate.getItem().getId());
	                    dto.setSubItemId(rate.getSubItem()!=null?rate.getSubItem().getId():null);
	                    dto.setUnit(rate.getUnit().getName());
	                    dto.setBalance(purchaseService.getAvailableBalance(rate.getItem().getId(), rate.getSubItem()==null?null:rate.getSubItem().getId(), rate.getUnit().getId(), issueDate==null?LocalDate.now():null));
	                    return dto;
	                })
	                .toList();
			}catch(Exception ex) {
			throw ex;
		}
    }
	
	public String createCategory(Category request) {
		try {
			if(categoryRepo.findByCodeOrName(request.getCode(), request.getName()).isPresent())
				throw new UnauthorizedException("Category/Code exists");
    	Category category  = new Category();
    	category.setName(request.getName());
    	category.setCode(request.getCode());
    	categoryRepo.save(category);
    	return "Added successfully";
		}catch(Exception ex) {
			throw ex;
		}
    }
}
