package com.synergizglobal.dms.repository.dms;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.dms.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long>{

	Optional<Department> findByName(String name);
	
}
