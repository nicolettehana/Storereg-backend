package sad.storereg.models.appdata;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "eproposal_requests", schema = "appdata")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class EProposalRequest {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code_o")
    private Integer outgoingUserCode;

    @Column(name = "user_designation_o")
    private String outgoingUserDesignation;

    @Column(name = "user_department_o")
    private String outgoingUserDepartment;
    
    @Column(name = "user_office_o")
    private String outgoingUserOffice;

    @Column(name = "app_no")
    private String appNo;

    @Column(name = "outgoing_timestamp", columnDefinition = "timestamp default now()")
    private LocalDateTime outgoingTimestamp;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "wl_no")
    private String wlNo;

    @Column(name = "quarter_no")
    private String quarterNo;

    @Column(name = "quarter_type")
    private String quarterType;

    @Column(name = "letter_no")
    private String letterNo;

    @Column(name = "memo_no")
    private String memoNo;

    @Column(name = "user_code_i")
    private Integer incomingUserCode;

    @Column(name = "user_designation_i")
    private String incomingUserDesignation;

    @Column(name = "user_department_i")
    private String incomingUserDepartment;
    
    @Column(name = "user_office_i")
    private String incomingUserOffice;

    @Column(name = "incoming_timestamp")
    private LocalDateTime incomingTimestamp;

    @Column(name = "status_timestamp")
    private LocalDateTime statusTimestamp;

    @Column(name = "status")
    private String status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "form_code")
    private UUID formCode;

    @Column(name = "user_name_i")
    private String incomingUserName;
    
    @Column(name = "user_name_o")
    private String outgoingUserName;

}
