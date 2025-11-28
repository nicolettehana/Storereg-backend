package sad.storereg.repo.master;

import org.springframework.data.jpa.repository.JpaRepository;

import sad.storereg.models.master.Departments;

public interface DepartmentsRepository extends JpaRepository<Departments, Integer>{
	
	Departments findByDeptCode(Integer deptCode);

}
