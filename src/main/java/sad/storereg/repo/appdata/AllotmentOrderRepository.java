package sad.storereg.repo.appdata;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.appdata.AllotmentOrders;

public interface AllotmentOrderRepository extends JpaRepository<AllotmentOrders, Long>{

}
