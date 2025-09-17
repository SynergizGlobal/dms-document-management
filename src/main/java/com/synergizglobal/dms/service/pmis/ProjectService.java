package com.synergizglobal.dms.service.pmis;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.synergizglobal.dms.dto.ProjectDTO;

public interface ProjectService {
	
	public List<ProjectDTO> getAllProjects();

	public List<ProjectDTO> getProjectsByUserId(String userId);

	public List<ProjectDTO> getProjects(String userId, String userRoleNameFk);


}
