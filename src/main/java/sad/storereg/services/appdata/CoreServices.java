package sad.storereg.services.appdata;

import java.util.Map;
import java.util.Optional;

import static sad.storereg.models.auth.Role.ADMIN;
import static sad.storereg.models.auth.Role.CH;
import static sad.storereg.models.auth.Role.USER;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import sad.storereg.exception.ObjectNotFoundException;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Blocks;
import sad.storereg.models.master.Departments;
import sad.storereg.models.master.Districts;
import sad.storereg.models.master.Offices;
import sad.storereg.models.master.QuarterPhysicalStatus;
import sad.storereg.models.master.QuarterTypes;
import sad.storereg.models.master.Villages;
import sad.storereg.repo.auth.UserRepository;
import sad.storereg.repo.master.ApplicationStatusRepository;
import sad.storereg.repo.master.BlocksRepository;
import sad.storereg.repo.master.DepartmentsRepository;
import sad.storereg.repo.master.DistrictsRepository;
import sad.storereg.repo.master.OfficesRepository;
import sad.storereg.repo.master.QuarterPhysicalStatusRepository;
import sad.storereg.repo.master.QuarterTypesRepository;
import sad.storereg.repo.master.VillagesRepository;

@Service
@RequiredArgsConstructor
public class CoreServices {
	
	private final ApplicationStatusRepository appStatusRepo;
	private final QuarterTypesRepository quarterTypesRepo;
	private final UserRepository userRepo;
	private final DepartmentsRepository deptRepo;
	private final OfficesRepository officeRepo;
	private final DistrictsRepository districtRepo;
	private final BlocksRepository blocksRepo;
	private final VillagesRepository villagesRepo;
	private final QuarterPhysicalStatusRepository physicalStatusRepo;
	
	public String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}
	
	public String convertEmail(String mail) {
		String email="";
		int indexOfAt = mail.indexOf("@");
		if (indexOfAt != -1) {
			email = mail.substring(0, indexOfAt + 1) // Include "@" in the substring
					+ mail.substring(indexOfAt + 1).replace(".", "[dot]");
			email = email.replace("@", "[at]");
		}
		return email;
	}
	
	public String getStatus(Integer statusCode) {
		return appStatusRepo.findByStatusCode(statusCode).getStatus();
	}
	
	public String getAction(Integer statusCode) {
		return appStatusRepo.findByStatusCode(statusCode).getAction();
	}
	
	public Map<String,Object> isValidFilename(String originalFileName) {
		Map<String, Object> responseMap = new HashMap<>();
		String metaCharactersRegex = "[\\p{Punct}&&[^._()\\-]]";
		String[] parts = originalFileName.split("\\.");
		
		if (originalFileName.contains("..")) {
			responseMap.put("detail", "Sorry! File name is containing invalid path sequence: " + originalFileName);
			responseMap.put("status", false);
		}
		else if (originalFileName.length() > 255) {
			responseMap.put("detail", "Sorry! File name is too long");
			responseMap.put("status", false);
		}
		
		else if (originalFileName.matches(".*" + metaCharactersRegex + ".*") || originalFileName.contains("%00")) {
			responseMap.put("detail", "Sorry! File name contains invalid characters");
			responseMap.put("status", false);
		}
		
		else if (parts.length > 2) {
			responseMap.put("detail", "Sorry! File name has double extension");
			responseMap.put("status", false);
		}
		else {
			responseMap.put("detail", "Accepted");
			responseMap.put("status", true);
		}
		return responseMap;
	}
	
	public String getQuarterType(String code) {
		QuarterTypes quarterType = quarterTypesRepo.findByCode(code).orElseThrow(()-> new ObjectNotFoundException("Invalid quarter code"));
		return quarterType.getQuarterType();
	}
	
	public String getRoleName(String username) {
		try {
			String roleName="-";
			
			Optional<User> user = userRepo.findByUsername(username);
			if(user.isPresent()) {
				if(user.get().getRole().equals(USER))
					roleName="Applicant";
				else if(user.get().getRole().equals(CH))
					roleName="Department";
				else if(user.get().getRole().equals(ADMIN))
					roleName="Admin";
			}
			
			return roleName;
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public String getRoleName(Long userCode) {
		try {
			String roleName="-";
			
			Optional<User> user = userRepo.findById(userCode);
			if(user.isPresent()) {
				if(user.get().getRole().equals(USER))
					roleName="Applicant";
				else if(user.get().getRole().equals(CH))
					roleName="Department";
				else if(user.get().getRole().equals(ADMIN))
					roleName="Admin";
			}
			
			return roleName;
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Departments getDepartment(Integer deptCode) {
		try {
			return deptRepo.findByDeptCode(deptCode);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Offices getOffice(Integer officeCode) {
		try {
			return officeRepo.findByOfficeCode(officeCode).orElse(null);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Districts getDistrict(Integer districtCode) {
		try {
			return districtRepo.findByLgdCode(districtCode).orElse(null);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Blocks getBlock(Integer blockCode) {
		try {
			return blocksRepo.findByBlockCode(blockCode).orElse(null);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public Villages getVillage(Integer villageCode) {
		try {
			return villagesRepo.findByVillageCode(villageCode).orElse(null);
		}catch(Exception ex) {
			throw ex;
		}
	}
	
	public QuarterPhysicalStatus getPhysicalStatus(Short physicalStatusCode) {
		try {
			return physicalStatusRepo.findById(physicalStatusCode).orElse(null);
		}catch(Exception ex) {
			throw ex;
		}
	}

}
