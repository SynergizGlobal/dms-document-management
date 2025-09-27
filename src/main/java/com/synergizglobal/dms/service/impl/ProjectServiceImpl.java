package com.synergizglobal.dms.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.ProjectDTO;
import com.synergizglobal.dms.entity.pmis.Project;
import com.synergizglobal.dms.repository.pmis.ProjectRepository;
import com.synergizglobal.dms.service.pmis.ProjectService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepository;
	
	
	@Override
	public List<ProjectDTO> getAllProjects() {
		// TODO Auto-generated method stub
		List<ProjectDTO> projectDTOs = new ArrayList<>();
		for (Project project : projectRepository.findAll()) {
			projectDTOs.add(ProjectDTO.builder()
					.id(project.getProjectName())
					.name(project.getProjectName())		
					.build());
		}
		return projectDTOs;
	}

	@Override
	public List<ProjectDTO> getProjectsByUserId(String userId) {
		List<ProjectDTO> projectDTOs = new ArrayList<>();
		for (String projectName : projectRepository.findByUserId(userId)) {
			projectDTOs.add(ProjectDTO.builder()
					.id(projectName)
					.name(projectName)		
					.build());
		}
		return projectDTOs;
	}

	@Override
	public List<ProjectDTO> getProjects(String userId, String userRoleNameFk) {
    	if(userRoleNameFk.equals("IT Admin")) {
    		//IT Admin
    		return this.getAllProjects();
    	} else if(userRoleNameFk.equals("Contractor")) {
    		return this.getProjectsByUserId(userId);
    	} else {
    		return this.getProjectsForOtherUsersByUserId(userId);
    	}
	}

	private List<ProjectDTO> getProjectsForOtherUsersByUserId(String userId) {
		List<ProjectDTO> projectDTOs = new ArrayList<>();
		for (String projectName : projectRepository.getProjectsForOtherUsersByUserId(userId)) {
			projectDTOs.add(ProjectDTO.builder()
					.id(projectName)
					.name(projectName)		
					.build());
		}
		return projectDTOs;
	}

}
