package com.synergizglobal.dms.controller.dms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.FolderDTO;
import com.synergizglobal.dms.dto.FolderGridDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.FolderService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

	private final FolderService folderService;

	public FolderController(FolderService folderService) {
		this.folderService = folderService;
	}

	@PostMapping("/grid")
	public ResponseEntity<List<FolderDTO>> getAllFolders(@RequestBody FolderGridDTO folderGridDto, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if(user.getUserRoleNameFk().equals("IT Admin")) {
    		//IT Admin
    		return ResponseEntity.ok(folderService.getAllFoldersByProjectsAndContracts(folderGridDto.getProjects(), folderGridDto.getContracts()));
    	}
		return ResponseEntity.ok(folderService.getAllFoldersByProjectsAndContracts(folderGridDto.getProjects(), folderGridDto.getContracts(), user.getUserId()));
	}
	
	@GetMapping("/get")
	public ResponseEntity<List<FolderDTO>> getAllFolders() {
		return ResponseEntity.ok(folderService.getAllFolders());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<FolderDTO> getFolderById(@PathVariable Long id) {
		return ResponseEntity.ok(folderService.getFolderById(id));
	}

	@PostMapping("/create")
	public ResponseEntity<FolderDTO> createFolder(@RequestBody FolderDTO folderDTO) {
		return ResponseEntity.ok(folderService.createFolder(folderDTO));
	}

	@PutMapping("/update-folder/{id}")
	public ResponseEntity<FolderDTO> updateFolder(@PathVariable Long id, @RequestBody FolderDTO folderDTO) {
		return ResponseEntity.ok(folderService.updateFolder(id, folderDTO));
	}

	@DeleteMapping("/delete-folder/{id}")
	public ResponseEntity<String> deleteFolder(@PathVariable Long id) {
		folderService.deleteFolder(id);
		return ResponseEntity.ok("Folder deleted successfully");
	}

	@DeleteMapping("/delete-subfolder/{id}")
	public ResponseEntity<String> deleteSubFolder(@PathVariable Long id) {
		folderService.deleteSubFolder(id);
		return ResponseEntity.ok("Subfolder deleted successfully");
	}

	@GetMapping("/get/name/{name}")
	public ResponseEntity<List<FolderDTO>> getFolderByName(@PathVariable("name") String name) {
		List<FolderDTO> list = new ArrayList<>();
		FolderDTO folderDto = folderService.getFolderByName(name);
		if (folderDto != null)
			list.add(folderDto);
		return ResponseEntity.ok(list);
	}
}
