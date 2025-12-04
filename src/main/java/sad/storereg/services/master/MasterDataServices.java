package sad.storereg.services.master;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.UnitRateDTO;
import sad.storereg.dto.master.UnitRequestDTO;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.Unit;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.RateRepository;
import sad.storereg.repo.master.UnitRepository;
import sad.storereg.repo.master.YearRangeRepository;

@Service
@RequiredArgsConstructor
public class MasterDataServices {
	
	private final CategoryRepository categoryRepo;
	
	private final UnitRepository unitRepo;
	
	private final RateRepository rateRepository;
	
	private final YearRangeRepository yearRangeRepository;
	
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
	                    return dto;
	                })
	                .toList();
			}catch(Exception ex) {
			throw ex;
		}
    }

}
