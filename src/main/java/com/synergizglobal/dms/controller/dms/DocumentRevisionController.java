package com.synergizglobal.dms.controller.dms;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.repository.dms.DocumentRevisionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documentversion")
@RequiredArgsConstructor
public class DocumentRevisionController {

	private final DocumentRevisionRepository documentRevisionRepository;
	
	@GetMapping("/get")
	List<com.synergizglobal.dms.dto.DocumentRevisionDTO> getAllVersions(@RequestParam("fileNumber") String fileNumber
			, @RequestParam("fileName") String fileName) {
		return documentRevisionRepository.findAllByFileNumberAndFileName(fileNumber, fileName);
	}
}
