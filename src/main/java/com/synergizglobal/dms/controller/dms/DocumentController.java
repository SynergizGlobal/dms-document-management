package com.synergizglobal.dms.controller.dms;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DataTableRequest;
import com.synergizglobal.dms.dto.DataTableResponse;
import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.DocumentFolderGridDTO;
import com.synergizglobal.dms.dto.DocumentGridDTO;
import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.dto.DraftSendDocumentDTO;
import com.synergizglobal.dms.dto.NotRequiredDTO;
import com.synergizglobal.dms.dto.SendDocumentDTO;
import com.synergizglobal.dms.entity.pmis.User;
//import com.synergizglobal.dms.service.dms.DepartmentService;
import com.synergizglobal.dms.service.dms.DocumentService;
import com.synergizglobal.dms.service.dms.SendDocumentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;

	private final SendDocumentService sendDocumentService;

	@PostMapping(consumes = { "multipart/form-data" })
	public ResponseEntity<?> uploadFileWithMetaData(@ModelAttribute DocumentDTO documentDto,
			@RequestParam("files") List<MultipartFile> files, HttpSession session) {
		try {
			User user = (User) session.getAttribute("user");
			String userId = user.getUserId();
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(documentService.uploadFileWithMetaData(documentDto, files, userId));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}

	}

	@GetMapping("/validate/fileName")
	public ResponseEntity<String> validateFileName(@RequestParam String fileName, @RequestParam String fileNumber) {
		String msg = documentService.validateFileName(fileName, fileNumber);
		return ResponseEntity.ok(msg);
	}

	@GetMapping("/get/filePath")
	public ResponseEntity<String> getFilePath(@RequestParam("fileName") String fileName,
			@RequestParam("fileNumber") String fileNumber, @RequestParam("revisionNo") String revisionNo) {
		String msg = documentService.getFilePath(fileName, fileNumber, revisionNo);
		return ResponseEntity.ok(msg);
	}

	@GetMapping("/validate/fileNumber")
	public ResponseEntity<String> validateFileNumber(@RequestParam String fileNumber, @RequestParam String fileName) {
		String msg = documentService.validateFileNumber(fileNumber, fileName);
		return ResponseEntity.ok(msg);
	}

	@GetMapping("/validate/revisionNumber")
	public ResponseEntity<String> validateRevisionNumber(@RequestParam String fileName, @RequestParam String fileNumber,
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

	@PostMapping("/filter-data")
	public ResponseEntity<DataTableResponse<DocumentGridDTO>> getFilteredDocuments(
			@RequestBody DataTableRequest request, HttpSession session) {
		// Parse pagination
		int start = request.getStart(); // Offset
		int length = request.getLength(); // Page size
		int draw = request.getDraw(); // Sync token
		User user = (User) session.getAttribute("user");
		Map<Integer, List<String>> columnFilters = request.getColumnFilters();
		List<DocumentGridDTO> paginated = documentService.getFilteredDocuments(columnFilters, start, length, user);
		long recordsFiltered = documentService.countFilteredDocuments(columnFilters, user);

		DataTableResponse<DocumentGridDTO> response = new DataTableResponse<>();
		response.setDraw(draw);
		response.setRecordsTotal(documentService.countAllFiles(user)); // Total in DB (optional: unfiltered)
		response.setRecordsFiltered(recordsFiltered); // After filtering
		response.setData(paginated);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/not-required")
	public ResponseEntity<Long> markNotRequired(@RequestBody NotRequiredDTO dto, HttpSession session) {
		User user = (User) session.getAttribute("user");
		documentService.markNotRequired(dto, user.getUserId());
		return ResponseEntity.ok(dto.getDocumentId());
	}
	@GetMapping("/filters/{columnIndex}")
	public ResponseEntity<List<String>> filters(@PathVariable("columnIndex") Integer columnIndex, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (columnIndex == 1) {
			return ResponseEntity.ok(documentService.findGroupedFileTypes(user.getUserId()));
		}
		if (columnIndex == 2) {
			return ResponseEntity.ok(documentService.findGroupedFileNumbers(user.getUserId()));
		}
		if (columnIndex == 3) {
			return ResponseEntity.ok(documentService.findGroupedFileNames(user.getUserId()));
		}
		if (columnIndex == 4) {
			return ResponseEntity.ok(documentService.findGroupedRevisionNos(user.getUserId()));
		}
		if (columnIndex == 5) {
			return ResponseEntity.ok(documentService.findGroupedStatus(user.getUserId()));
		}
		if (columnIndex == 6) {
			return ResponseEntity.ok(documentService.findGroupedProjectNames(user.getUserId()));
		}
		if (columnIndex == 7) {
			return ResponseEntity.ok(documentService.findGroupedContractNames(user.getUserId()));
		}
		if (columnIndex == 8) {
			return ResponseEntity.ok(documentService.findGroupedFolders(user.getUserId()));
		}
		if (columnIndex == 9) {
			return ResponseEntity.ok(documentService.findGroupedSubFolders(user.getUserId()));
		}
		if (columnIndex == 10) {
			return ResponseEntity.ok(documentService.findGroupedCreatedBy(user.getUserId()));
		}
		if (columnIndex == 11) {
			return null;// ResponseEntity.ok(documentService.findGroupedUploadedDate());
		}
		if (columnIndex == 12) {
			return ResponseEntity.ok(documentService.findGroupedRevisionDate(user.getUserId()));
		}
		if (columnIndex == 13) {
			return ResponseEntity.ok(documentService.findGroupedDepartment(user.getUserId()));
		}
		return null;
	}

	@GetMapping("/view")
	public ResponseEntity<Resource> viewFile(@RequestParam("path") String path) throws IOException {
		java.nio.file.Path filePath = java.nio.file.Paths.get(path);
		Resource resource = new UrlResource(filePath.toUri());

		if (!resource.exists()) {
			return ResponseEntity.notFound().build();
		}

		String mimeType = java.nio.file.Files.probeContentType(filePath);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(resource);
	}
	
	
	@GetMapping("/folder-grid/{subfolderId}")
	public List<DocumentFolderGridDTO> getFilesForFolderGrid(@PathVariable("subfolderId") String subfolderId, HttpSession session) throws IOException {
		User user = (User) session.getAttribute("user");
		return documentService.getFilesForFolderGrid(subfolderId, user.getUserId());
	}
	
	@GetMapping("/archived/folder-grid/{subfolderId}")
	public List<DocumentFolderGridDTO> getArvhivedFilesForFolderGrid(@PathVariable("subfolderId") String subfolderId, HttpSession session) throws IOException {
		User user = (User) session.getAttribute("user");
		return documentService.getArvhivedFilesForFolderGrid(subfolderId, user.getUserId());
	}
	
	@GetMapping("/download")
	public ResponseEntity<Resource> downloadFile(@RequestParam("path") String path) throws IOException {
		java.nio.file.Path filePath = java.nio.file.Paths.get(path);
		Resource resource = new UrlResource(filePath.toUri());

		if (!resource.exists()) {
			return ResponseEntity.notFound().build();
		}

		String fileName = filePath.getFileName().toString();
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}

	@PostMapping("/send-document")
	public ResponseEntity<String> saveOrSendDocument(@RequestBody SendDocumentDTO dto, HttpSession session,
			HttpServletRequest request) throws IOException {
		User user = (User) session.getAttribute("user");
		String baseUrl = request.getScheme() + "://" + // http / https
				request.getServerName() + // domain or IP
				":" + request.getServerPort() + // port
				request.getContextPath(); // context path
		String response = documentService.saveOrSendDocument(dto, user.getUserId(), baseUrl);
		return ResponseEntity.ok("");
	}

	@PostMapping("/drafts")
	public DraftDataTableResponse<DraftSendDocumentDTO> getDrafts(@RequestBody DraftDataTableRequest request,
			HttpSession session) {
		User user = (User) session.getAttribute("user");
		return sendDocumentService.getDrafts(request, user.getUserId());
	}
}