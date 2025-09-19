package com.synergizglobal.dms.controller.dms;

import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergizglobal.dms.controller.pmis.ContractController;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/correspondence")
@Slf4j
@RequiredArgsConstructor
public class CorrespondenceController {

	
	
    private final ContractController contractController;


    private final ICorrespondenceService correspondenceService;
    private final ObjectMapper objectMapper;

	

    @PostMapping(value = "/uploadLetter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLetter(
            @RequestPart("dto") String dtoJson,
            @RequestParam("document") MultipartFile[] documentsArray) {

        try {
            CorrespondenceUploadLetter dto =
                    objectMapper.readValue(dtoJson, CorrespondenceUploadLetter.class);

            dto.setDocuments(Arrays.asList(documentsArray));

          
            CorrespondenceLetter savedLetter = correspondenceService.saveLetter(dto);

            return ResponseEntity.ok("Letter uploaded successfully: " + savedLetter.getLetterNumber());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload letter: " + e.getMessage());
        }}

    @GetMapping("/getCorrespondeneceList")
    public ResponseEntity<List<CorrespondenceLetterProjection>> getCorrespondeneceList(
            @RequestParam String action) {
        return ResponseEntity.ok(correspondenceService.getLettersByAction(action));
    }


    @GetMapping("/getReferenceLetters")
    public ResponseEntity<List<String>> getReferenceLetters(
            @RequestParam(required = false) String query) {

        List<String> letters = correspondenceService.findReferenceLetters(query);

        if (letters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(letters);
    }


    @GetMapping("/view/{id}")
    public ResponseEntity<CorrespondenceLetterViewDto> getCorrespondenceWithFiles(@PathVariable Long id, HttpServletRequest request) {
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


}



