package com.synergizglobal.dms.controller.dms;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DocumentDTO;
//import com.synergizglobal.dms.service.dms.DepartmentService;
import com.synergizglobal.dms.service.dms.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
	
	private final DocumentService documentService;

	@PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFileWithMetaData(
    		@ModelAttribute DocumentDTO documentDto,
    		@RequestParam("files") List<MultipartFile> files) {
		try {
            return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadFileWithMetaData(documentDto, files));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }
	
}