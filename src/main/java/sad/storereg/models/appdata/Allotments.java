package sad.storereg.models.appdata;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="allotments", schema="appdata")
public class Allotments {
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Id
	private Long id;
	
	@Column(name="quarter_no")
	private String quarterNo;
	
	@Column(name="appno")
	private String appNo;
	
	@Column(name="occupation_date")
	private LocalDate occupationDate;
	
	@Column(name="vacate_date")
	private LocalDate vacateDate;
	
	private Date entrydate;
	
	@Column(name="user_code")
	private Long userCode;
	
	@Column(name="is_approved")
	private Integer isApproved;
	
	@Column(name="cs_decision_timestamp")
	private Date csDecisionTimestamp;

	@Column(name="cs_remark")
	private String csRemarks;
	
	@Column(name="prev_wl")
	private Integer prevWL;
	
	@Column(name="prev_wl_no")
	private Integer prevWLno;
	
	@Column(name="prev_wl_entry_timestamp")
	private Date prevWLTimestamp;
	
	@Column(name="e_sign_secy")
	private Integer eSignSecy;

	@Column(name="secy_timestamp")
	private Date secyTimestamp;
	
	@Column(name="e_sign_under_secy")
	private Integer esignUnderSecy;
	
	@Column(name="under_secy_timestamp")
	private Date underSecyTimestamp;
	
	@Column(name="letter_no")
	private String letterNo;
	
	@Column(name="memo_no")
	private String memoNo;
	
	private String filename;
	
	@Column(name="applicant_accept")
	private Integer applicantAccept;
	
	@Column(name="applicant_accept_timestamp")
	private Date applicantAcceptTimestamp;
	
	@Column(name="applicant_letter")
	private String applicantLetter;

}
