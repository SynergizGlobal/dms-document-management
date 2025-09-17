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
					.id(project.getProjectId())
					.name(project.getProjectId())		
					.build());
		}
		return projectDTOs;
	}

}
