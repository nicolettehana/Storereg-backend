package sad.storereg.services.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sad.storereg.models.master.Item;
import sad.storereg.repo.master.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

    public Page<Item> getAll(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Item create(Item item) {
        return itemRepository.save(item);
    }
}
