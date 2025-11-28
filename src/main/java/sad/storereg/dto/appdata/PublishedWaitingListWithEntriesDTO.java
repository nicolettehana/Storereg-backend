package sad.storereg.dto.appdata;

import java.util.List;

import lombok.Data;
import sad.storereg.models.appdata.PublishedWaitingList;

@Data
public class PublishedWaitingListWithEntriesDTO {
	private PublishedWaitingList publishedList;
    private List<WaitingListEntryDTO> entries;
}
