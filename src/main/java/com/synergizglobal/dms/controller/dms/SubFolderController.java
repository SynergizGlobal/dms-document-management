package com.synergizglobal.dms.controller.dms;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.entity.dms.SubFolder;
import com.synergizglobal.dms.service.dms.SubFolderService;

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

}