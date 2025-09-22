package com.synergizglobal.dms.controller.dms;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.FolderGridDTO;
import com.synergizglobal.dms.entity.dms.SubFolder;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.SubFolderService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subfolders")
@RequiredArgsConstructor
public class SubFolderController {
	
	private final SubFolderService subFolderService;
	
	@GetMapping("/{folderId}")
    public ResponseEntity<List<SubFolder>> getSubFoldersByFolderId(@PathVariable("folderId") Long folderId) {
        return ResponseEntity.ok(subFolderService.getSubFoldersByFolderId(folderId));
    }
	
	@PostMapping("/grid/{folderId}")
    public ResponseEntity<List<SubFolder>> getsubfolderGridByFolderId(@PathVariable("folderId") Long folderId,@RequestBody FolderGridDTO folderGridDto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user.getUserRoleNameFk().equals("IT Admin")) {
    		//IT Admin
    		return ResponseEntity.ok(subFolderService.getAllSubfolderGridByFolderId(folderId, folderGridDto.getProjects(), folderGridDto.getContracts()));
    	}
		return ResponseEntity.ok(subFolderService.getsubfolderGridByFolderId(folderId, user.getUserId(), folderGridDto.getProjects(), folderGridDto.getContracts()));
    }

}