package sad.storereg.models.appdata;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "published_waiting_list_entries", schema = "appdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishedWaitingListEntry {
	
	@Id
	@JsonProperty(access = Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_list_id", nullable = false)
    @JsonBackReference
    private PublishedWaitingList publishedList;

    @Column(name = "app_no", nullable = false)
    private String appNo;

    @Column(name = "sl_no", nullable = false)
    private Integer slNo;

}
