package com.synergizglobal.dms.controller.pmis;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.ProjectDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.DocumentService;
import com.synergizglobal.dms.service.pmis.ProjectService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private final DocumentService documentService;
    
    @GetMapping("/get")
    public ResponseEntity<List<ProjectDTO>> getAllProjects(HttpSession session) {
    	User user = (User) session.getAttribute("user");

    	return ResponseEntity.ok(projectService.getProjects(user.getUserId(), user.getUserRoleNameFk()));
    }
    
    
    @GetMapping("/get/for-folder-grid")
    public ResponseEntity<List<String>> findGroupedProjectNames(HttpSession session) {
    	User user = (User) session.getAttribute("user");
    	if(user.getUserRoleNameFk().equals("IT Admin")) {
    		//IT Admin
    		return ResponseEntity.ok(documentService.findAllProjectNamesByDocument());
    	}
    	return ResponseEntity.ok(documentService.findGroupedProjectNames(user.getUserId()));
    }
}
