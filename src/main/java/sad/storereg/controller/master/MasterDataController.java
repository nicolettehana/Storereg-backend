package sad.storereg.controller.master;

import java.util.List;

import static sad.storereg.models.auth.Role.ADMIN;

import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sad.storereg.dto.master.DepartmentOfficeDTO;
import sad.storereg.models.auth.User;
import sad.storereg.models.master.Blocks;
import sad.storereg.models.master.Departments;
import sad.storereg.models.master.Districts;
import sad.storereg.models.master.Offices;
import sad.storereg.models.master.Villages;
import sad.storereg.repo.auth.UserRepository;
import sad.storereg.repo.master.BlocksRepository;
import sad.storereg.repo.master.DepartmentsRepository;
import sad.storereg.repo.master.DistrictsRepository;
import sad.storereg.repo.master.OfficesRepository;
import sad.storereg.repo.master.VillagesRepository;

@RestController
@RequiredArgsConstructor
public class MasterDataController {
	
	private final DepartmentsRepository deptRepo;
	private final OfficesRepository officeRepo;
	private final UserRepository userRepo;
	private final DistrictsRepository districtsRepo;
	private final BlocksRepository blocksRepo;
	private final VillagesRepository villagesRepo;
	
	@GetMapping("/departments")
	public List<DepartmentOfficeDTO> getDepartments(@AuthenticationPrincipal User user) throws Exception {
		try {
			List<DepartmentOfficeDTO> list= new ArrayList<>();
			if(user.getRole().equals(ADMIN)) {
				List<Departments> depts = deptRepo.findAll();
				for (Departments dept : depts) {
	                DepartmentOfficeDTO dto = new DepartmentOfficeDTO();
	                dto.setDeptCode(dept.getDeptCode());
	                dto.setDeptName(dept.getDeptName());
	                dto.setOffices(dept.getOffices());
	                list.add(dto);
	            }
			}
			else {
				User userr = userRepo.findByUsername(user.getUsername()).orElseThrow();
				if(userr.getOfficeCode()==null) {
					throw new BadRequestException("User is not associated with a department");
				}
				Offices office = officeRepo.findById(userr.getOfficeCode()).orElseThrow();
				DepartmentOfficeDTO dept = new DepartmentOfficeDTO();
				Departments deptt = deptRepo.findByDeptCode(office.getDepartment().getDeptCode());
				dept.setDeptCode(deptt.getDeptCode());
				dept.setDeptName(deptt.getDeptName());
				dept.setOffices(deptt.getOffices());
				list.add(dept);
				
			}
			return list;
		}
		catch(Exception ex){
			throw ex;
		}	
	}
	
	@GetMapping("districts")
	public List<Districts> getDistricts() {
		try {
			return districtsRepo.findAll();
		}
		catch(Exception ex){
			throw ex;
		}	
	}
	
	@GetMapping("blocks/{district_code}")
	public List<Blocks> getBlocks(@PathVariable("district_code") Integer districtCode) {
		try {
			return blocksRepo.findAllByDistrictCode(districtCode);
		}
		catch(Exception ex){
			throw ex;
		}	
	}
	
	@GetMapping("villages/{block_code}")
	public List<Villages> getVillages(@PathVariable("block_code") Integer blockCode) {
		try {
			return villagesRepo.findAllByBlockCode(blockCode);
		}
		catch(Exception ex){
			throw ex;
		}	
	}

}
