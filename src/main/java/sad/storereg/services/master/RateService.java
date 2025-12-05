package sad.storereg.services.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.ItemDTO;
import sad.storereg.dto.master.ItemRateCreateDTO;
import sad.storereg.dto.master.ItemRateDTO;
import sad.storereg.dto.master.SubItemRateDTO;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.Category;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.SubItems;
import sad.storereg.models.master.Unit;
import sad.storereg.models.master.YearRange;
import sad.storereg.repo.master.CategoryRepository;
import sad.storereg.repo.master.ItemRepository;
import sad.storereg.repo.master.RateRepository;
import sad.storereg.repo.master.UnitRepository;
import sad.storereg.repo.master.YearRangeRepository;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class RateService {

	private final RateRepository rateRepository;
    private final ItemRepository itemRepository;
    private final YearRangeRepository yearRangeRepository;
    private final UnitRepository unitRepository;
    private final CategoryRepository categoryRepository;
    
    public Page<ItemRateDTO> getRates(String category, Integer yearRangeId, Pageable pageable) {

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

            // --- ITEM RATE (subItem == null) ---
            if (rate.getSubItem() == null) {

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


            // --- SUB-ITEM RATE ---
            else {

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

                itemSubItemRateMap.get(item.getId()).put(
                    sub.getId(),
                    new SubItemRateDTO(
                        sub.getId(),
                        sub.getName(),
                        rate.getUnit().getUnit(),
                        rate.getRate()
                    )
                );

                // If sub-item rates exist, item-level rate does not apply
                grouped.get(item.getId()).setUnit(null);
                grouped.get(item.getId()).setRate(null);
            }
        }


        // --- SECOND PASS: add missing subItems ---
        for (ItemRateDTO dto : grouped.values()) {

            // find a rate that belongs to this item
            Rate exampleRate = ratePage.getContent().stream()
                .filter(r -> r.getItem() != null && r.getItem().getId().equals(dto.getId()))
                .findFirst()
                .orElse(null);

            if (exampleRate == null) continue;

            Item item = exampleRate.getItem();

            for (SubItems sub : item.getSubItems()) {

                Map<Long, SubItemRateDTO> existingSubMap =
                        itemSubItemRateMap.getOrDefault(dto.getId(), new HashMap<>());

                if (!existingSubMap.containsKey(sub.getId())) {

                    // No rate defined for this subItem
                    dto.getSubItems().add(
                        new SubItemRateDTO(
                            sub.getId(),
                            sub.getName(),
                            null,
                            null
                        )
                    );
                } else {

                    dto.getSubItems().add(existingSubMap.get(sub.getId()));
                }
            }
        }


        List<ItemRateDTO> dtoList = new ArrayList<>(grouped.values());
        return new PageImpl<>(dtoList, pageable, ratePage.getTotalElements());
    }


//	public Page<ItemRateDTO> getRates2(String category, Integer yearRangeId, Pageable pageable){
//		
//		Page<Rate> ratePage;
//
//        // ---- FILTER PRIORITY ----
//        if (category != null && yearRangeId != null) {
//            ratePage = rateRepository.findByCategory_CodeAndYearRange_Id(category, yearRangeId, pageable);
//        } 
//        else if (category != null) {
//            ratePage = rateRepository.findByCategory_Code(category, pageable);
//        } 
//        else if (yearRangeId != null) {
//            ratePage = rateRepository.findByYearRange_Id(yearRangeId, pageable);
//        } 
//        else {
//            ratePage = rateRepository.findAll(pageable);
//        }
//        System.out.println("Rate: "+ratePage.getContent());
//        Map<Long, ItemRateDTO> grouped = new LinkedHashMap<>();
//        Map<Long, Map<Long, SubItemRateDTO>> itemSubItemRateMap = new HashMap<>();
//
//        // --- FIRST PASS: process all rates ---
//        for (Rate rate : ratePage.getContent()) {
//
//            if (rate.getObjectType().equals("item")) {
//
//                Item item = rate.getItem();
//
//                grouped.putIfAbsent(
//                    item.getId(),
//                    new ItemRateDTO(
//                        item.getId(),
//                        item.getName(),
//                        item.getCategory().getName(),
//                        rate.getYearRange().getStartYear(),
//                        rate.getYearRange().getEndYear(),
//                        null, null,
//                        new ArrayList<>()
//                    )
//                );
//
//                grouped.get(item.getId()).setUnit(rate.getUnit().getUnit());
//                grouped.get(item.getId()).setRate(rate.getRate());
//            }
//
//            else if (rate.getObjectType().equals("subItem")) {
//
//                SubItems sub = rate.getSubItem();
//                Item item = sub.getItem();
//
//                grouped.putIfAbsent(
//                    item.getId(),
//                    new ItemRateDTO(
//                        item.getId(),
//                        item.getName(),
//                        item.getCategory().getName(),
//                        rate.getYearRange().getStartYear(),
//                        rate.getYearRange().getEndYear(),
//                        null, null,
//                        new ArrayList<>()
//                    )
//                );
//
//                itemSubItemRateMap.putIfAbsent(item.getId(), new HashMap<>());
//
//                // store the rate for this subItem
//                itemSubItemRateMap.get(item.getId()).put(
//                    sub.getId(),
//                    new SubItemRateDTO(
//                        sub.getId(),
//                        sub.getName(),
//                        rate.getUnit().getUnit(),
//                        rate.getRate()
//                    )
//                );
//
//                grouped.get(item.getId()).setUnit(null);
//                grouped.get(item.getId()).setRate(null);
//            }
//        }
//
//        // --- SECOND PASS: add missing subItems (unit = null, rate = null) ---
//        for (ItemRateDTO dto : grouped.values()) {
//
//            // find the original item
//            Rate exampleRate = ratePage.getContent().stream()
//                .filter(r -> r.getItem() != null && r.getItem().getId().equals(dto.getId()))
//                .findFirst()
//                .orElse(null);
//
//            if (exampleRate == null) continue;
//
//            Item item = exampleRate.getItem();
//
//            // iterate item.subItems()
//            for (SubItems sub : item.getSubItems()) {
//
//                Map<Long, SubItemRateDTO> existing = itemSubItemRateMap.getOrDefault(dto.getId(), new HashMap<>());
//
//                if (!existing.containsKey(sub.getId())) {
//                    // subItem has NO rate → add null unit & rate
//                    dto.getSubItems().add(
//                        new SubItemRateDTO(
//                            sub.getId(),
//                            sub.getName(),
//                            null,
//                            null
//                        )
//                    );
//                } else {
//                    // subItem has a rate → add it
//                    dto.getSubItems().add(existing.get(sub.getId()));
//                }
//            }
//        }
//
//        List<ItemRateDTO> dtoList = new ArrayList<>(grouped.values());
//        return new PageImpl<>(dtoList, pageable, ratePage.getTotalElements());
//    }
//	
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
	
	public String createRate(ItemRateCreateDTO request) {
		
		YearRange yearRange = yearRangeRepository.findById(request.getYearRangeId())
                .orElseThrow(() -> new RuntimeException("YearRange not found"));

		SubItems subItemm=null;
		Item item = itemRepository.findById(request.getItemId())
				.orElseThrow(() -> new RuntimeException("Item not found"));
		if(request.getSubItemId()!=null) {
			boolean exists=false;
			
			for(SubItems subItem:item.getSubItems()) {
				if(subItem.getId()==request.getSubItemId()) {
					subItemm = subItem;
					exists=true;
				}
			}
			if(!exists)
				throw new RuntimeException("Sub-item not found");
		}		
        
        Category category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        Unit unit = unitRepository.findById(request.getUnitId())
        		.orElseThrow(()-> new RuntimeException("Unit not found"));
        if(request.getSubItemId()==null) {
        	if(rateRepository.findByItem_IdAndSubItemIsNullAndYearRange_Id(request.getItemId(), request.getYearRangeId()).isPresent())
        		throw new RuntimeException("Rate already present");
        }
        else {
        	if(rateRepository.findByItem_IdAndSubItem_IdAndYearRange_Id(request.getItemId(), request.getSubItemId(), request.getYearRangeId()).isPresent())
				throw new RuntimeException("Rate already present");
        }
        
        		//.findByObjectTypeAndObjectIdAndYearRange_Id(request.getSubItemId()==null?"item":"subItem", 
        		//request.getSubItemId()==null?item.getId():request.getSubItemId(), request.getYearRangeId()).isPresent())
        	
        Rate itemRate = new Rate();

        //itemRate.setObjectType(request.getSubItemId()==null?"item":"subItem");
        //itemRate.setObjectId(request.getSubItemId()==null?item.getId():request.getSubItemId());
        //if(request.getSubItemId()==null)
        itemRate.setItem(item);
        if(request.getSubItemId()!=null)
        	itemRate.setSubItem(subItemm);
        itemRate.setYearRange(yearRange);
        itemRate.setUnit(unit);
        itemRate.setCategory(category);
        itemRate.setRate(request.getRate());
        itemRate.setEntryDate(LocalDateTime.now());

        rateRepository.save(itemRate);
		return("Rate added");
	}
	
	public Double getRate(Integer unitId, Long itemId, Long subItemId, Integer yearRangeId) {
		
		Rate rate = rateRepository.findRates(itemId, subItemId, yearRangeId, unitId).orElseThrow(() -> new ObjectNotFoundException("Rate not available"));
		return rate.getRate();
	}
}
