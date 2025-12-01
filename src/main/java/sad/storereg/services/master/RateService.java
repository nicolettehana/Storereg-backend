package sad.storereg.services.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.ItemRateDTO;
import sad.storereg.dto.master.SubItemRateDTO;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.SubItems;
import sad.storereg.repo.master.RateRepository;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class RateService {

	private final RateRepository rateRepository;


	public Page<ItemRateDTO> getRates(String category, Integer yearRangeId, Pageable pageable){
		
		Page<Rate> ratePage;

        // ---- FILTER PRIORITY ----
        if (category != null && yearRangeId != null) {
            ratePage = rateRepository.findByCategory_CodeAndYearRange_Id(category, yearRangeId, pageable);
        } 
        else if (category != null) {
            ratePage = rateRepository.findByCategory_Code(category, pageable);
        } 
        else if (yearRangeId != null) {
            ratePage = rateRepository.findByYearRange_Id(yearRangeId, pageable);
        } 
        else {
            ratePage = rateRepository.findAll(pageable);
        }
        
        Map<Long, ItemRateDTO> grouped = new LinkedHashMap<>();
        Map<Long, Map<Long, SubItemRateDTO>> itemSubItemRateMap = new HashMap<>();

        // --- FIRST PASS: process all rates ---
        for (Rate rate : ratePage.getContent()) {

            if (rate.getObjectType().equals("item")) {

                Item item = rate.getItem();

                grouped.putIfAbsent(
                    item.getId(),
                    new ItemRateDTO(
                        item.getId(),
                        item.getName(),
                        item.getCategory().getName(),
                        rate.getYearRange().getStartYear(),
                        rate.getYearRange().getEndYear(),
                        null, null,
                        new ArrayList<>()
                    )
                );

                grouped.get(item.getId()).setUnit(rate.getUnit().getUnit());
                grouped.get(item.getId()).setRate(rate.getRate());
            }

            else if (rate.getObjectType().equals("subItem")) {

                SubItems sub = rate.getSubItem();
                Item item = sub.getItem();

                grouped.putIfAbsent(
                    item.getId(),
                    new ItemRateDTO(
                        item.getId(),
                        item.getName(),
                        item.getCategory().getName(),
                        rate.getYearRange().getStartYear(),
                        rate.getYearRange().getEndYear(),
                        null, null,
                        new ArrayList<>()
                    )
                );

                itemSubItemRateMap.putIfAbsent(item.getId(), new HashMap<>());

                // store the rate for this subItem
                itemSubItemRateMap.get(item.getId()).put(
                    sub.getId(),
                    new SubItemRateDTO(
                        sub.getId(),
                        sub.getName(),
                        rate.getUnit().getUnit(),
                        rate.getRate()
                    )
                );

                grouped.get(item.getId()).setUnit(null);
                grouped.get(item.getId()).setRate(null);
            }
        }

        // --- SECOND PASS: add missing subItems (unit = null, rate = null) ---
        for (ItemRateDTO dto : grouped.values()) {

            // find the original item
            Rate exampleRate = ratePage.getContent().stream()
                .filter(r -> r.getItem() != null && r.getItem().getId().equals(dto.getId()))
                .findFirst()
                .orElse(null);

            if (exampleRate == null) continue;

            Item item = exampleRate.getItem();

            // iterate item.subItems()
            for (SubItems sub : item.getSubItems()) {

                Map<Long, SubItemRateDTO> existing = itemSubItemRateMap.getOrDefault(dto.getId(), new HashMap<>());

                if (!existing.containsKey(sub.getId())) {
                    // subItem has NO rate → add null unit & rate
                    dto.getSubItems().add(
                        new SubItemRateDTO(
                            sub.getId(),
                            sub.getName(),
                            null,
                            null
                        )
                    );
                } else {
                    // subItem has a rate → add it
                    dto.getSubItems().add(existing.get(sub.getId()));
                }
            }
        }

        List<ItemRateDTO> dtoList = new ArrayList<>(grouped.values());
        return new PageImpl<>(dtoList, pageable, ratePage.getTotalElements());
    }
	
	public Page<Rate> getRates1(String category, Integer yearRangeId, Pageable pageable){
		
		if (category != null && yearRangeId != null) {
	        return rateRepository.findByCategory_CodeAndYearRange_Id(category, yearRangeId, pageable);
	    }

	    if (category != null) {
	        return rateRepository.findByCategory_Code(category, pageable);
	    }

	    if (yearRangeId != null) {
	        return rateRepository.findByYearRange_Id(yearRangeId, pageable);
	    }

	    return rateRepository.findAll(pageable);
	}
}
