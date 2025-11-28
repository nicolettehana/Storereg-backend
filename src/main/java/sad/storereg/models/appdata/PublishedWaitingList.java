package sad.storereg.models.appdata;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sad.storereg.dto.appdata.WaitingListEntryDTO;

@Entity
@Table(name = "published_waiting_lists", schema = "appdata",
       uniqueConstraints = @UniqueConstraint(columnNames = {"list_type", "version"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishedWaitingList {

	@Id
	@JsonProperty(access = Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entrydate", nullable = false)
    private LocalDateTime entryDate = LocalDateTime.now();

    @Column(name = "list_type", nullable = false)
    private String listCode;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "published_by")
    private String publishedBy;

    private String remarks;
    
    @Column(name="is_legacy")
	private Integer isLegacy;

    @JsonProperty(access = Access.WRITE_ONLY)
    @OneToMany(mappedBy = "publishedList", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PublishedWaitingListEntry> entries;
}
