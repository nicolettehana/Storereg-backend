package sad.storereg.services.master;

import java.util.List;

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
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.FirmCategoryRepository;
import sad.storereg.repo.master.FirmsRepository;

@Service
@RequiredArgsConstructor
public class FirmService {
	
	private final FirmsRepository firmRepository;
	private final FirmCategoryRepository firmCategoryRepository;
	private final CategoryRepository categoryRepository;

    public Page<FirmsDTO> getFirms(Pageable pageable, String search, String category) {
    	Page<Firm> page = null;
    	if(category==null || category.equals("") || category.equals("All"))
    		page = firmRepository.findAll(pageable);
//    	else
//    		page = firmRepository.findAllByCategory_Code(category, pageable);
    	else {
            // Must query via FirmCategoryRepository
            Page<FirmCategory> fcPage = firmCategoryRepository.findByCategory_Code(category, pageable);
            // Extract unique firms
            List<Firm> firms = fcPage.stream()
                                     .map(FirmCategory::getFirm)
                                     .distinct()
                                     .toList();
            page = new PageImpl<>(firms, pageable, fcPage.getTotalElements());
        }
System.out.println(page.getContent());
        return page.map(this::convertToDto);
    }

    private FirmsDTO convertToDto(Firm firm) {
        return FirmsDTO.builder()
            .id(firm.getId())
            .firm(firm.getFirm())
            .categories(
                firm.getCategories().stream()
                    .map(FirmCategory::getCategory) // <-- direct Category entity
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

}
