package com.synergizglobal.dms.controller.dms;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	@GetMapping("/validate/fileName")
    public ResponseEntity<String> validateFileName(@RequestParam String fileName, @RequestParam String fileNumber) {
        String msg = documentService.validateFileName(fileName, fileNumber);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/fileNumber")
    public ResponseEntity<String> validateFileNumber(@RequestParam String fileNumber, @RequestParam String fileName) {
        String msg = documentService.validateFileNumber(fileNumber, fileName);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/revisionNumber")
    public ResponseEntity<String> validateRevisionNumber(@RequestParam String fileName,
                                                         @RequestParam String fileNumber,
                                                         @RequestParam String revisionNo) {
        String msg = documentService.validateRevisionNumber(fileName, fileNumber, revisionNo);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/folder")
    public ResponseEntity<String> validateFolder(@RequestParam String folderName) {
        String msg = documentService.validateFolder(folderName);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/subFolder")
    public ResponseEntity<String> validateSubFolder(@RequestParam String folderName,
                                                    @RequestParam String subFolderName) {
        String msg = documentService.validateSubFolder(folderName, subFolderName);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/department")
    public ResponseEntity<String> validateDepartment(@RequestParam String departmentName) {
        String msg = documentService.validateDepartment(departmentName);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/status")
    public ResponseEntity<String> validateStatus(@RequestParam String statusName) {
        String msg = documentService.validateStatus(statusName);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/validate/revisionDate")
    public ResponseEntity<String> validateRevisionDate(@RequestParam String revisionDate) {
        String msg = documentService.validateRevisionDate(revisionDate);
        return ResponseEntity.ok(msg);
    }
	
}