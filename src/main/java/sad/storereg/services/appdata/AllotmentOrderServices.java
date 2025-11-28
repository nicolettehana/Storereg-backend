package sad.storereg.services.appdata;

import java.util.Date;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.models.appdata.AcceptanceRejectionLetters;
import sad.storereg.models.appdata.AllotmentOrders;
import sad.storereg.repo.appdata.AcceptanceRejectionLettersRepository;
import sad.storereg.repo.appdata.AllotmentOrderRepository;

@Service
@RequiredArgsConstructor
public class AllotmentOrderServices {
	
	private final AllotmentOrderRepository allotmentOrderRepo;
	private final AcceptanceRejectionLettersRepository applicantLettersRepo;
	
	@Transactional
	public void saveAllotmentOrder(Long allotmentId, String code, String path, String extension, Integer step, String username, Date entrydate) {
		try {
			AllotmentOrders order = AllotmentOrders.builder().allotmentId(allotmentId)
					.code(code).path(path).extension(extension).step(step).username(username).entrydate(entrydate)
					.build();
			allotmentOrderRepo.save(order);
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void saveApplicantLetter(Long allotmentId, char code, String path, String extension, String username, Date entrydate) {
		try {
			AcceptanceRejectionLetters letter = AcceptanceRejectionLetters.builder().allotmentId(allotmentId)
					.code(code).path(path).extension(extension).username(username).entrydate(entrydate).build();
			
			applicantLettersRepo.save(letter);
		}
		catch(Exception ex) {
			throw ex;
		}
	}

}
