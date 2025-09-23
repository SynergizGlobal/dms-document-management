package com.synergizglobal.dms.controller.dms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.synergizglobal.dms.dto.CorrespondenceGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.dto.DataTableRequest;
import com.synergizglobal.dms.dto.DataTableResponse;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/correspondence")
@Slf4j
@RequiredArgsConstructor
public class CorrespondenceController {


    private final ICorrespondenceService correspondenceService;
    private final ObjectMapper objectMapper;


    @PostMapping(value = "/uploadLetter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLetter(
            @RequestPart("dto") String dtoJson,
            @RequestParam("document") MultipartFile[] documentsArray, HttpServletRequest request) {

        try {
            CorrespondenceUploadLetter dto =
                    objectMapper.readValue(dtoJson, CorrespondenceUploadLetter.class);

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
                if (f.getFileName() != null && !f.getFileName().isBlank()) {
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()                       // includes context path e.g. http://host:port/dms
                            .path("/api/correspondence/files/")            // controller mapping
                            .pathSegment(f.getFileName())                  // encodes filename safely
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

        CorrespondenceLetterViewDto dto = correspondenceService.getCorrespondenceWithFilesByLetterNumber(letterNumber);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        String origin = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        if (dto.getFiles() != null) {
            dto.getFiles().forEach(f -> {
                if (f.getFileName() != null && !f.getFileName().isBlank()) {
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
		List<CorrespondenceGridDTO> paginated = correspondenceService.getFilteredCorrespondence(columnFilters, start, length, user);
		long recordsFiltered = correspondenceService.countFilteredCorrespondence(columnFilters, user);

		DataTableResponse<CorrespondenceGridDTO> response = new DataTableResponse<>();
		response.setDraw(draw);
		response.setRecordsTotal(correspondenceService.countAllCorrespondence(user)); // Total in DB (optional: unfiltered)
		response.setRecordsFiltered(recordsFiltered); // After filtering
		response.setData(paginated);

		return ResponseEntity.ok(response);
	}

}
