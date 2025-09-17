package com.synergizglobal.dms.controller.pmis;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.ProjectDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.pmis.ProjectService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/get")
    public ResponseEntity<List<ProjectDTO>> getAllProjects(HttpSession session) {
    	User user = (User) session.getAttribute("user");

    	return ResponseEntity.ok(projectService.getProjects(user.getUserId(), user.getUserRoleNameFk()));
    }
}
