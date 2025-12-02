package sad.storereg.repo.appdata;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sad.storereg.models.appdata.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>{
	
	@Query("""
	        SELECT DISTINCT p 
	        FROM Purchase p
	        JOIN FETCH p.firm f
	        JOIN FETCH p.items pi
	        JOIN FETCH pi.item i
	        JOIN FETCH i.category c
	        LEFT JOIN FETCH pi.subItem si
	        WHERE p.date BETWEEN :startDate AND :endDate
	        AND (:category IS NULL OR c.code = :category)
	        AND (
	            :searchValue IS NULL 
	            OR LOWER(f.firm) LIKE LOWER(CONCAT('%', :searchValue, '%'))
	            OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchValue, '%'))
	            OR LOWER(si.name) LIKE LOWER(CONCAT('%', :searchValue, '%'))
	        )
	    """)
	    List<Purchase> searchPurchasesList(LocalDate startDate,
	                                   LocalDate endDate,
	                                   String category,
	                                   String searchValue);
	
	@EntityGraph(attributePaths = {
            "firm",
            "items",
            "items.item",
            "items.item.category",
            "items.subItem"
    })
    @Query("""
        SELECT p FROM Purchase p
        JOIN p.firm f
        JOIN p.items pi
        JOIN pi.item i
        WHERE p.date BETWEEN :startDate AND :endDate
        AND (:category IS NULL OR i.category.code = :category)
        AND (
            :searchValue IS NULL
            OR LOWER(f.firm) LIKE LOWER(CONCAT('%', :searchValue, '%'))
            OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchValue, '%'))
        )
    """)
    Page<Purchase> searchPurchases(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String searchValue,
            Pageable pageable
    );

}
