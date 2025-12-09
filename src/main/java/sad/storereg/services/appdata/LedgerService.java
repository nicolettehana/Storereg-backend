package sad.storereg.services.appdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.appdata.LedgerResponse;
import sad.storereg.dto.appdata.LedgerSubItemResponse;
import sad.storereg.dto.appdata.LedgerUnitResponse;
import sad.storereg.models.appdata.StockBalance;
import sad.storereg.models.master.Item;
import sad.storereg.models.master.Rate;
import sad.storereg.models.master.SubItems;
import sad.storereg.repo.appdata.IssueRepository;
import sad.storereg.repo.appdata.PurchaseRepository;
import sad.storereg.repo.appdata.StockBalanceRepository;
import sad.storereg.repo.master.ItemRepository;
import sad.storereg.repo.master.RateRepository;

@Service
@RequiredArgsConstructor
public class LedgerService {
	
	private final ItemRepository itemRepo;
    private final PurchaseRepository purchaseRepo;
    private final IssueRepository issueRepo;
    private final StockBalanceRepository stockRepo;
    private final RateRepository rateRepo;

 // Main entry: returns paged ledger
    @Transactional(readOnly = true)
    public Page<LedgerResponse> getLedger(LocalDate startDate,
                                          LocalDate endDate,
                                          Integer yearRangeId,
                                          String categoryCode,
                                          Pageable pageable) {

    	Page<Item> itemsPage = itemRepo.findAll(pageable);
    	System.out.println("Category Code: "+categoryCode);
    	if (categoryCode != null && categoryCode.length()>0) {
            itemsPage = itemRepo.findAllByCategory_Code(categoryCode, pageable);
        } if(categoryCode==null || categoryCode.equals("")){
            itemsPage = itemRepo.findAll(pageable);
        }
        
        System.out.println("Items Page: "+itemsPage.getContent());

        List<LedgerResponse> dtoList = new ArrayList<>(itemsPage.getContent().size());

        for (Item item : itemsPage.getContent()) {
            LedgerResponse ledger = new LedgerResponse();
            ledger.setItemId(item.getId());
            ledger.setItemName(item.getName());
            ledger.setCategory(item.getCategory() != null ? item.getCategory().getName() : null);
            ledger.setCategoryCode(item.getCategory() != null ? item.getCategory().getCode() : null);

            boolean hasSubItems = item.getSubItems() != null && !item.getSubItems().isEmpty();

            if (!hasSubItems) {
                // get all rates (units) for item (subItem = null)
                List<Rate> rates = rateRepo.findRatesByItemAndOptionalSubItem(item.getId(), null, yearRangeId);
                Set<Integer> unitIdsAdded = new HashSet<>();

                for (Rate r : rates) {
                    if (r.getUnit() == null) continue;
                    Integer unitId = r.getUnit().getId();
                    // avoid duplicates if multiple rate rows exist for same unit
                    if (unitIdsAdded.contains(unitId)) continue;
                    unitIdsAdded.add(unitId);

                    LedgerUnitResponse u = computeUnitLedger(item.getId(), null,
                            unitId, r.getUnit().getName(), startDate, endDate);

                    ledger.getUnits().add(u);
                }

                // If there were no rates found, optionally we could still compute units
                // by looking at actual transactions' unit ids. (Edge case)
                if (ledger.getUnits().isEmpty()) {
                    // discover units from transactions for this item (optional step)
                    // Skipped here for brevity; recommended to add if needed.
                }

            } else {
                // for each subitem, gather rates (units) and compute per unit
                for (SubItems si : item.getSubItems()) {
                    LedgerSubItemResponse subDto = new LedgerSubItemResponse();
                    subDto.setSubItemId(si.getId());
                    subDto.setSubItemName(si.getName());

                    List<Rate> rates = rateRepo.findRatesByItemAndOptionalSubItem(item.getId(), si.getId(), yearRangeId);
                    Set<Integer> unitIdsAdded = new HashSet<>();

                    for (Rate r : rates) {
                        if (r.getUnit() == null) continue;
                        Integer unitId = r.getUnit().getId();
                        if (unitIdsAdded.contains(unitId)) continue;
                        unitIdsAdded.add(unitId);

                        LedgerUnitResponse u = computeUnitLedger(item.getId(), si.getId(),
                                unitId, r.getUnit().getName(), startDate, endDate);

                        subDto.getUnits().add(u);
                    }

                    // if no rates found, optional fallback to units seen in transactions

                    ledger.getSubItems().add(subDto);
                }
            }

            dtoList.add(ledger);
        }

        return new PageImpl<>(dtoList, pageable, itemsPage.getTotalElements());
    }

    // Helper: compute opening (before startDate), purchases and issues between start & end
    private LedgerUnitResponse computeUnitLedger(Long itemId, Long subItemId,
                                                 Integer unitId, String unitName,
                                                 LocalDate startDate, LocalDate endDate) {

        // opening = purchases_until(startDate-1) - issues_until(startDate-1)
        LocalDate openingEnd = startDate.minusDays(1);

        Integer purchasesBefore = purchaseRepo.sumQtyByItemSubItemUnitUntil(itemId, subItemId, unitId, openingEnd);
        Integer issuesBefore = issueRepo.sumQtyByItemSubItemUnitUntil(itemId, subItemId, unitId, openingEnd);

        int opening = (purchasesBefore == null ? 0 : purchasesBefore) - (issuesBefore == null ? 0 : issuesBefore);

        // purchases in range
        Integer purchasesInRange = purchaseRepo.sumQtyByItemSubItemUnitBetween(itemId, subItemId, unitId, startDate, endDate);
        Integer issuesInRange = issueRepo.sumQtyByItemSubItemUnitBetween(itemId, subItemId, unitId, startDate, endDate);

        int purchased = purchasesInRange == null ? 0 : purchasesInRange;
        int issued = issuesInRange == null ? 0 : issuesInRange;

        int closing = opening + purchased - issued;

        LedgerUnitResponse u = new LedgerUnitResponse();
        u.setUnitId(unitId);
        u.setUnitName(unitName);
        u.setOpeningBalance(opening);
        u.setNoOfPurchases(purchased);
        u.setNoOfIssues(issued);
        u.setClosingBalance(closing);

        return u;
    }
}
