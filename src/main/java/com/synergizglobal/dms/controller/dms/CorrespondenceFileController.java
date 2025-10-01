
package com.synergizglobal.dms.controller.dms;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.common.CommonUtil;
import com.synergizglobal.dms.dto.CorrespondenceFileDTO;
import com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO;
import com.synergizglobal.dms.dto.FolderGridDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.CorrespondenceFileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/correspondence")
public class CorrespondenceFileController {

	private final Path storageRoot;
	@Autowired
	private CorrespondenceFileService fileService;

	public CorrespondenceFileController(@Value("${file.upload-dir}") String storagePath) {
		this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
	}

	@GetMapping("/files/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		try {
			Path filePath = storageRoot.resolve(filename).normalize();
			if (!filePath.startsWith(storageRoot) || !Files.exists(filePath)) {
				return ResponseEntity.notFound().build();
			}

			Resource resource = new UrlResource(filePath.toUri());
			String contentType = Files.probeContentType(filePath);
			if (contentType == null)
				contentType = "application/octet-stream";

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(
					HttpHeaders.CONTENT_DISPOSITION,
					"inline; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
					.body(resource);

		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/getFolderFiles")
	public ResponseEntity<List<CorrespondenceFolderFileDTO>> getFiles(@RequestBody FolderGridDTO folderGridDto, @RequestParam("type") String type,
			HttpServletRequest request,HttpSession session) {
		String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();

		//HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		List<CorrespondenceFolderFileDTO> files = new ArrayList<>();
		if (CommonUtil.isITAdminOrSuperUser(user)) {
			if (type.equals("Incoming"))
				files = fileService.getFilesForAdminIncoming(folderGridDto.getProjects(), folderGridDto.getContracts(), type, baseUrl);
			else
				files = fileService.getFilesForAdminOutgoing(folderGridDto.getProjects(), folderGridDto.getContracts(), type, baseUrl);
		} else {
			if (type.equals("Incoming"))
				files = fileService.getFilesIncoming(folderGridDto.getProjects(), folderGridDto.getContracts(), type, baseUrl, user.getUserId());
			else
				files = fileService.getFilesOutgoing(folderGridDto.getProjects(), folderGridDto.getContracts(), type, baseUrl, user.getUserId());
		}
		// Set download URL only if file exists
		files.forEach(file -> {
			Path filePath = storageRoot.resolve(file.getFilePath()).normalize();
			file.setDownloadUrl(
					Files.exists(filePath) ? baseUrl + "/api/correspondence/files/" + file.getFilePath() : null);
		});

		return ResponseEntity.ok(files);
	}

	@GetMapping("/files/**")
	public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
		try {
			// Extract relative path after /files/
			String relativePath = request.getRequestURI().substring(request.getRequestURI().indexOf("/files/") + 7);

			Path filePath = storageRoot.resolve(relativePath).normalize();

			if (!filePath.startsWith(storageRoot) || !Files.exists(filePath)) {
				return ResponseEntity.notFound().build();
			}

			Resource resource = new UrlResource(filePath.toUri());
			String contentType = Files.probeContentType(filePath);
			if (contentType == null)
				contentType = "application/octet-stream";

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(
					HttpHeaders.CONTENT_DISPOSITION,
					"inline; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
					.body(resource);

		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	
	  @GetMapping("/files-id/{correspondenceId}")
	    public ResponseEntity<List<CorrespondenceFileDTO>> getFiles(@PathVariable("correspondenceId") Long correspondenceId) {
		  //System.out.print("Called");
	        List<CorrespondenceFileDTO> files = fileService.getFilesByCorrespondenceId(correspondenceId);
	        return ResponseEntity.ok(files);
	    }

}
