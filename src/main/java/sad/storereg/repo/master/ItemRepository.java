package sad.storereg.repo.master;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Item;

public interface ItemRepository extends JpaRepository<Item, Long>{

}
