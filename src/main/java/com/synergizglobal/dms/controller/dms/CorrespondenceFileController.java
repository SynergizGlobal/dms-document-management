

package com.synergizglobal.dms.controller.dms;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO;
import com.synergizglobal.dms.service.dms.CorrespondenceFileService;

import jakarta.servlet.http.HttpServletRequest;

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
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    @GetMapping("/getFolderFiles")
    public ResponseEntity<List<CorrespondenceFolderFileDTO>> getFiles(
            @RequestParam List<String> projectNames,
            @RequestParam List<String> contractNames,
            @RequestParam String type,
            HttpServletRequest request
    ) {
        // Build base URL of your app
        String baseUrl = request.getScheme() + "://" 
                + request.getServerName() 
                + ":" + request.getServerPort() 
                + request.getContextPath();

        // File download URL base
        String fileBaseUrl = baseUrl + "/api/correspondence/files/";

        // Get metadata from DB
        List<CorrespondenceFolderFileDTO> files =
                fileService.getFiles(projectNames, contractNames, type, fileBaseUrl);

        // Check against storageRoot and fix URLs
        files.forEach(file -> {
            Path filePath = storageRoot.resolve(file.getFilePath()).normalize(); // use DB path
            if (Files.exists(filePath)) {
                // build correct download URL including folder
                file.setDownloadUrl(fileBaseUrl + file.getFilePath());
            } else {
                // file missing in storage folder
                file.setDownloadUrl(null);
            }
        });

        return ResponseEntity.ok(files);
    }
    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        try {
            // Extract relative path after /files/
            String relativePath = request.getRequestURI()
                    .substring(request.getRequestURI().indexOf("/files/") + 7);

            Path filePath = storageRoot.resolve(relativePath).normalize();

            if (!filePath.startsWith(storageRoot) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
