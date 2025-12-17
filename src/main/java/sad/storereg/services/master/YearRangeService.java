package sad.storereg.services.master;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.master.YearRangeRepository;

@RequiredArgsConstructor
@Service
public class YearRangeService {

	private final YearRangeRepository yearRangeRepository;

    public List<YearRange> getAllYearRanges() {
    	try {
    		return yearRangeRepository.findAll();
    	}catch(Exception ex) {
    		throw ex;
    	}
    }
    
    public String createYearRange(YearRange request) {
    	YearRange yearRange  = new YearRange();
    	yearRange.setStartYear(request.getStartYear());
    	yearRange.setEndYear(request.getEndYear());
    	yearRange.setEntryDate(LocalDateTime.now());
    	yearRangeRepository.save(yearRange);
    	return "Added successfully";
    }
}
