package sad.storereg.repo.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sad.storereg.dto.master.QuarterOccupancyStatusDTO;
import sad.storereg.models.master.QuarterPhysicalOccupancy;
import sad.storereg.models.master.QuarterStatusRelationId;

public interface QuarterPhysicalOccupancyRepository extends JpaRepository<QuarterPhysicalOccupancy, QuarterStatusRelationId>{
	
	List<QuarterPhysicalOccupancy> findById_PhysicalStatus(Integer physicalStatus);
	
	@Query("""
	        SELECT new sad.storereg.dto.master.QuarterOccupancyStatusDTO(qs.statusCode, qs.quarterStatus)
	        FROM QuarterPhysicalOccupancy qpo
	        JOIN QuarterStatus qs ON qpo.occupancyStatus = qs.statusCode
	        WHERE qpo.id.physicalStatus = :physicalStatusCode
	    """)
	    List<QuarterOccupancyStatusDTO> findOccupancyStatusesByPhysicalStatus(@Param("physicalStatusCode") Integer physicalStatusCode);

}
