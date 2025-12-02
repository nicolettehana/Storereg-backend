package sad.storereg.services.master;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.CreateFirmDTO;
import sad.storereg.dto.master.FirmsDTO;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Firm;
import sad.storereg.models.master.FirmCategory;
import sad.storereg.models.master.FirmYear;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.FirmCategoryRepository;
import sad.storereg.repo.master.FirmYearRepository;
import sad.storereg.repo.master.FirmsRepository;

@Service
@RequiredArgsConstructor
public class FirmService {
	
	private final FirmsRepository firmRepository;
	private final FirmCategoryRepository firmCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final FirmYearRepository firmYearRepository;

	public Page<FirmsDTO> getFirms(Pageable pageable, String search, String category) {
	    Page<Firm> page;

	    if (category == null || category.isEmpty() || category.equals("All")) {
	        page = firmRepository.findAll(pageable);
	    } else {
	        // Query firms by category through FirmCategory
	        Page<FirmCategory> fcPage = firmCategoryRepository.findByCategory_Code(category, pageable);

	        // Extract unique firms
	        List<Firm> firms = fcPage.stream()
	                .map(FirmCategory::getFirm)
	                .distinct()
	                .toList();

	        page = new PageImpl<>(firms, pageable, fcPage.getTotalElements());
	    }

	    return page.map(this::convertToDto);
	}
	
	private FirmsDTO convertToDto(Firm firm) {

	    // Fetch FirmYear rows for this firm
	    List<FirmYear> firmYears = firmYearRepository.findByFirm_Id(firm.getId());

	    return FirmsDTO.builder()
	            .id(firm.getId())
	            .firm(firm.getFirm())

	            // All categories belonging to this Firm
	            .categories(
	                firm.getCategories().stream()
	                    .map(FirmCategory::getCategory)
	                    .toList()
	            )

	            // YearRanges from FirmYear table
	            .yearRanges(
	                firmYears.stream()
	                        .map(FirmYear::getYearRange)
	                        .distinct()
	                        .toList()
	            )

	            .build();
	}
	
    @Transactional
    public String createFirm(CreateFirmDTO request) {
    	
    	Firm firm = Firm.builder()
    			.firm(request.getFirmName()).build();
    	firmRepository.save(firm);
    	
    	for (String code : request.getCategories()) {
    		// Get category entity by code
            Category category = categoryRepository.findById(code)
                    .orElseThrow(() -> new RuntimeException("Category code not found: " + code));
    	    FirmCategory firmCategory = FirmCategory.builder().firm(firm).category(category).build();
    	    firmCategoryRepository.save(firmCategory);
    	}
        return "Firm added";
    }
    
    public Page<FirmsDTO> getFirms_old(Integer yearRangeId, String categoryCode, Pageable pageable) {

        Page<FirmYear> firmYears;

        if (categoryCode != null && !categoryCode.isEmpty()) {
            firmYears = firmYearRepository.findByYearRange_IdAndCategory_Code(
                    yearRangeId, categoryCode, pageable
            );
        } else {
            firmYears = firmYearRepository.findByYearRange_Id(
                    yearRangeId, pageable
            );
        }

        // Build DTO list using ONLY category & yearRange from FirmYear
        List<FirmsDTO> dtos = firmYears.getContent()
                .stream()
                .map(fy -> {
                    FirmsDTO dto = new FirmsDTO();
                    dto.setId(fy.getFirm().getId());
                    dto.setFirm(fy.getFirm().getFirm());

                    // Only the category from this firm-year record
                    dto.setCategories(List.of(fy.getCategory()));

                    // Only the year range from this firm-year record
                    dto.setYearRanges(List.of(fy.getYearRange()));

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, firmYears.getTotalElements());
    }
    
    public Page<FirmsDTO> getFirms(Integer yearRangeId, String categoryCode, Pageable pageable) {

        Page<FirmYear> firmYears;

        if (categoryCode != null && !categoryCode.isEmpty()) {
            firmYears = firmYearRepository.findByYearRange_IdAndCategory_Code(
                    yearRangeId, categoryCode, pageable
            );
        } else {
            firmYears = firmYearRepository.findByYearRange_Id(
                    yearRangeId, pageable
            );
        }

        // --- GROUP RESULT BY FIRM ---
        Map<Long, FirmsDTO> dtoMap = new LinkedHashMap<>();

        for (FirmYear fy : firmYears.getContent()) {

            Long firmId = fy.getFirm().getId();

            // Create DTO only once per firm
            dtoMap.computeIfAbsent(firmId, id -> {
                FirmsDTO dto = new FirmsDTO();
                dto.setId(id);
                dto.setFirm(fy.getFirm().getFirm());
                dto.setCategories(new ArrayList<>());
                dto.setYearRanges(new ArrayList<>());
                return dto;
            });

            FirmsDTO dto = dtoMap.get(firmId);

            // Add category if not already present
            if (!dto.getCategories().contains(fy.getCategory())) {
                dto.getCategories().add(fy.getCategory());
            }

            // Add yearRange if not already present
            if (!dto.getYearRanges().contains(fy.getYearRange())) {
                dto.getYearRanges().add(fy.getYearRange());
            }
        }

        // Convert map to list
        List<FirmsDTO> mergedList = new ArrayList<>(dtoMap.values());

        // Pagination metadata stays from FirmYear page
        return new PageImpl<>(mergedList, pageable, firmYears.getTotalElements());
    }

    public List<FirmsDTO> getFirmsList() {
        // Fetch all firms
        List<Firm> firms = firmRepository.findAll();

        // Convert to DTO
        return firms.stream()
                .map(this::convertToDto)
                .toList();
    }

}
