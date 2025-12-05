package sad.storereg.repo.appdata;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sad.storereg.models.appdata.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long>{
	
	@Query("""
	        SELECT iss FROM Issue iss
	        JOIN iss.items it
	        WHERE iss.date BETWEEN :startDate AND :endDate
	        AND (:category IS NULL OR it.categoryCode = :category)
	        AND (
	            :searchValue IS NULL
	            OR LOWER(iss.issueTo) LIKE LOWER(CONCAT('%', :searchValue, '%'))
	            OR LOWER(iss.remarks) LIKE LOWER(CONCAT('%', :searchValue, '%'))
	        )
	    """)
	Page<Issue> searchIssues(
	        LocalDate startDate,
	        LocalDate endDate,
	        String category,
	        String searchValue,
	        Pageable pageable
	);

}
