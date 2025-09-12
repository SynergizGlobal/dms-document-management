package com.synergizglobal.dms.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.DepartmentDTO;
import com.synergizglobal.dms.entity.dms.Department;
import com.synergizglobal.dms.repository.dms.DepartmentRepository;
import com.synergizglobal.dms.service.dms.DepartmentService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

	
	  private final DepartmentRepository departmentRepository;

	  @Override
	    public DepartmentDTO createDepartment(DepartmentDTO dto) {
	        if (departmentRepository.findByName(dto.getName()).isPresent()) {
	            throw new IllegalArgumentException("Department already exists with name: " + dto.getName());
	        }
	        Department department = Department.builder().name(dto.getName()).build();
	        Department saved = departmentRepository.save(department);
	        return mapToDTO(saved);
	    }

	  @Override
	    public List<DepartmentDTO> getAllDepartments() {
	        return departmentRepository.findAll()
	                .stream()
	                .map(this::mapToDTO)
	                .collect(Collectors.toList());
	    }

	  @Override
		public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
		    Department department = departmentRepository.findById(id)
		            .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
		
		    departmentRepository.findByName(dto.getName()).ifPresent(existing -> {
		        if (!existing.getId().equals(id)) {
		            throw new IllegalArgumentException("Another department already exists with name: " + dto.getName());
		        }
		    });
		
		    department.setName(dto.getName());
		    Department updated = departmentRepository.save(department);
		    return mapToDTO(updated);
		}
	   
	  @Override
	    public void deleteDepartment(Long id) {
	        if (!departmentRepository.existsById(id)) {
	            throw new RuntimeException("Department not found with id: " + id);
	        }
	        departmentRepository.deleteById(id);
	    }

	    private DepartmentDTO mapToDTO(Department department) {
	        return DepartmentDTO.builder()
	                .id(department.getId())
	                .name(department.getName())
	                .build();
	    }

}
