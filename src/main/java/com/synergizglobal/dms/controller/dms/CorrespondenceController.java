package com.synergizglobal.dms.controller.dms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergizglobal.dms.dto.CorrespondenceDraftGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.dto.DataTableRequest;
import com.synergizglobal.dms.dto.DataTableResponse;
import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.dms.SendCorrespondenceLetter;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;


@RestController
@RequestMapping("/api/correspondence")
@Slf4j
@RequiredArgsConstructor
public class CorrespondenceController {

    @Value("${file.upload-dir}") 
	private String uploadDir;
    private final ICorrespondenceService correspondenceService;
    private final ObjectMapper objectMapper;


    @GetMapping("/files")
    public ResponseEntity<Resource> serveFileByPath(@RequestParam("path") String path,
                                                    HttpServletRequest request) {
        try {
            // Spring already decodes query params, so 'path' is safe to use directly
            // Normalize and prevent path traversal
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolved = base.resolve(path).normalize();

            // Security check: ensure resolved path is inside uploadDir
            if (!resolved.startsWith(base)) {
                log.warn("Attempt to access file outside upload dir: {}", path);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            log.info("Serving file by path. uploadDir='{}', resolved='{}'", base, resolved);

            if (!Files.exists(resolved) || !Files.isReadable(resolved)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(resolved.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Malformed URL for path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error serving file by path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/filess/name/{filename:.+}")
    public ResponseEntity<Resource> serveFileByName(@PathVariable String filename,
                                                    HttpServletRequest request) {
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolved = base.resolve("Correspondence").resolve(filename).normalize();

            if (!resolved.startsWith(base)) {
                log.warn("Attempt to access file outside upload dir by name: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            log.info("Serving file by name. resolved='{}'", resolved);

            if (!Files.exists(resolved) || !Files.isReadable(resolved)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(resolved.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("Malformed URL for filename: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            log.error("IO error serving filename: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/uploadLetter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLetter(
            @RequestPart("dto") String dtoJson,
            @RequestParam(value = "document", required = false) MultipartFile[] documentsArray,
            HttpServletRequest request) {

        try {
            CorrespondenceUploadLetter dto =
                    objectMapper.readValue(dtoJson, CorrespondenceUploadLetter.class);

            if(documentsArray != null)
            	dto.setDocuments(Arrays.asList(documentsArray));
            String baseUrl = request.getScheme() + "://" + // http / https
                    request.getServerName() + // domain or IP
                    ":" + request.getServerPort() + // port
                    request.getContextPath(); // context path


            HttpSession session = request.getSession(false);
            String loggedUserId = null;
            String loggedUserName = null;
            String userRole= null;
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    loggedUserId = user.getUserId();       // String as per your User entity
                    loggedUserName = user.getUserName();
                    userRole= user.getUserRoleNameFk();
                }
            }

            // optional: require login
            if (loggedUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
            CorrespondenceLetter savedLetter = correspondenceService.saveLetter(dto, baseUrl, loggedUserId, loggedUserName,userRole);

            return ResponseEntity.ok("Letter uploaded successfully: " + savedLetter.getLetterNumber());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload letter: " + e.getMessage());
        }
    }

    @GetMapping("/getCorrespondeneceList")
    public ResponseEntity<List<CorrespondenceLetterProjection>> getCorrespondeneceList(
            @RequestParam("action") String action) {
        return ResponseEntity.ok(correspondenceService.getLettersByAction(action));
    }

    @GetMapping("/get/{correspondenceId}")
    public ResponseEntity<CorrespondenceLetter> getCorrespondeneceById(
            @PathVariable("correspondenceId") Long correspondenceId) {
        return ResponseEntity.ok(correspondenceService.getCorrespondeneceById(correspondenceId));
    }
    
    @GetMapping("/get/sendcorrespondence/{correspondenceId}")
    public ResponseEntity<List<SendCorrespondenceLetter>> getSendCorrespondeneceById(
            @PathVariable("correspondenceId") Long correspondenceId) {
        return ResponseEntity.ok(correspondenceService.getSendCorrespondeneceById(correspondenceId));
    }

    @GetMapping("/getReferenceLetters")
    public ResponseEntity<List<String>> getReferenceLetters(
            @RequestParam(required = false, name="query") String query) {

        List<String> letters = correspondenceService.findReferenceLetters(query);

        if (letters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(letters);
    }


    @GetMapping("/view/{id}")
    public ResponseEntity<CorrespondenceLetterViewDto> getCorrespondenceWithFiles(@PathVariable("id") Long id, HttpServletRequest request) {
        CorrespondenceLetterViewDto dto = correspondenceService.getCorrespondenceWithFiles(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        String origin = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        if (dto.getFiles() != null) {
            dto.getFiles().forEach(f -> {
                if (f.getFilePath() != null && !f.getFilePath().isBlank()) {
                    // DON'T pre-encode
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/correspondence/files")
                            .queryParam("path", f.getFilePath())
                            .toUriString();
                    f.setDownloadUrl(url);
                } else if (f.getFileName() != null && !f.getFileName().isBlank()) {
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/correspondence/files/")
                            .pathSegment(f.getFileName())
                            .toUriString();
                    f.setDownloadUrl(url);
                }
            });
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/search")
    public ResponseEntity<List<CorrespondenceLetter>> filter(@RequestBody CorrespondenceLetter letter) {

        return ResponseEntity.ok(correspondenceService.getFiltered(letter));
    }

    @GetMapping("/filter/column")
    public ResponseEntity<?> getDynamic(@RequestParam String fields, @RequestParam(defaultValue = "true") boolean distinct) {

        List<String> selectedFields = Arrays.stream(fields.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        List<Map<String, Object>> rows = correspondenceService.fetchDynamic(selectedFields, distinct);

        // If only one column requested → return flat list
        if (selectedFields.size() == 1) {
            return ResponseEntity.ok(rows.stream()
                    .map(m -> m.get(selectedFields.get(0)))
                    .toList());
        }

        return ResponseEntity.ok(rows);
    }

    public ResponseEntity<List<Object>> searchColumn(@RequestParam String fieldName) {
        return null;
    }

    @GetMapping("/view/letter/{letterNumber}")
    public ResponseEntity<CorrespondenceLetterViewDto> getCorrespondenceWithFilesByLetterNumber(
            @PathVariable String letterNumber,
            HttpServletRequest request) {

        System.out.print("View letter called");
        CorrespondenceLetterViewDto dto = correspondenceService.getCorrespondenceWithFilesByLetterNumber(letterNumber);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        String origin = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

     // inside getCorrespondenceWithFilesByLetterNumber(...)
        if (dto.getFiles() != null) {
            dto.getFiles().forEach(f -> {
                if (f.getFilePath() != null && !f.getFilePath().isBlank()) {
                    // DON'T pre-encode
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/correspondence/files")
                            .queryParam("path", f.getFilePath())
                            .toUriString();
                    f.setDownloadUrl(url);
                } else if (f.getFileName() != null && !f.getFileName().isBlank()) {
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/correspondence/files/")
                            .pathSegment(f.getFileName())
                            .toUriString();
                    f.setDownloadUrl(url);
                }
            });
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/search/letter")
    public ResponseEntity<List<CorrespondenceLetter>> searchLetter(@RequestBody CorrespondenceLetter letter) {

        return ResponseEntity.ok(correspondenceService.search(letter));
    }

    @PostMapping("/filter-data")
    public ResponseEntity<DataTableResponse<CorrespondenceGridDTO>> getFilteredDocuments(
            @RequestBody DataTableRequest request, HttpSession session) {
        // Parse pagination
        int start = request.getStart(); // Offset
        int length = request.getLength(); // Page size
        int draw = request.getDraw(); // Sync token
        User user = (User) session.getAttribute("user");
        Map<Integer, List<String>> columnFilters = request.getColumnFilters();
        List<CorrespondenceGridDTO> paginated = correspondenceService.getFilteredCorrespondenceNative(columnFilters, start, length, user);
        long recordsFiltered = correspondenceService.getFilteredCorrespondenceNativeCount(columnFilters, user);

        DataTableResponse<CorrespondenceGridDTO> response = new DataTableResponse<>();
        response.setDraw(draw);
        response.setRecordsTotal(correspondenceService.countAllCorrespondence(user)); // Total in DB (optional: unfiltered)
        response.setRecordsFiltered(recordsFiltered); // After filtering
        response.setData(paginated);

		return ResponseEntity.ok(response);
	}
@PostMapping("/drafts")
public DraftDataTableResponse<CorrespondenceDraftGridDTO> getDrafts(@RequestBody DraftDataTableRequest request,
		HttpSession session) {
	User user = (User) session.getAttribute("user");
	return correspondenceService.getDrafts(request, user.getUserId());
}
    @GetMapping("/filters/{columnIndex}")
    public ResponseEntity<List<String>> filters(@PathVariable("columnIndex") Integer columnIndex, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (columnIndex == 1) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllCategory());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedCategory(user.getUserId()));
        }
        if (columnIndex == 2) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllLetterNumbers());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedLetterNumbers(user.getUserId()));
        }
        if (columnIndex == 3) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllFrom());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedFrom(user.getUserId()));
        }
        if (columnIndex == 4) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllToSend());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedToSend(user.getUserId()));
        }
        if (columnIndex == 5) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllSubject());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedSubject(user.getUserId()));
        }
        if (columnIndex == 6) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllRequiredResponse());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedRequiredResponse(user.getUserId()));
        }
        if (columnIndex == 7) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllDueDates());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedDueDates(user.getUserId()));
        }
        if (columnIndex == 8) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllProjectNames());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedProjectNames(user.getUserId()));
        }
        if (columnIndex == 9) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllContractNames());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedContractNames(user.getUserId()));
        }
        if (columnIndex == 10) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllStatus());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedStatus(user.getUserId()));
        }
        if (columnIndex == 11) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllDepartment());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedDepartment(user.getUserId()));
        }
        if (columnIndex == 12) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllAttachment());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedAttachment(user.getUserId()));
        }
        if (columnIndex == 13) {
            if (user.getUserRoleNameFk().equals("IT Admin")) {
                // IT Admin
                return ResponseEntity.ok(correspondenceService.findAllTypesOfMail());
            }
            return ResponseEntity.ok(correspondenceService.findGroupedTypesOfMail(user.getUserId()));
        }
        return null;
    }


}
