package com.synergizglobal.dms.controller.dms;

import java.util.Arrays;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;

@RestController
@RequestMapping("/api/correspondence")
@Slf4j
@RequiredArgsConstructor
public class CorrespondenceController {


    private final ICorrespondenceService correspondenceService;
    private final ObjectMapper objectMapper;
	/*
	 * public CorrespondenceController(ObjectMapper objectMapper) {
	 * 
	 * this.objectMapper = objectMapper; }
	 */
	/*
	 * @PostMapping(value = "/uploadLetter", consumes =
	 * MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<String>
	 * uploadLetter(
	 * 
	 * @RequestPart("dto") String dtoJson,
	 * 
	 * @RequestParam("document") MultipartFile[] documentsArray) { // Change to
	 * array try {
	 * 
	 * ObjectMapper mapper = new ObjectMapper(); CorrespondenceUploadLetter dto =
	 * mapper.readValue(dtoJson, CorrespondenceUploadLetter.class);
	 * 
	 * 
	 * List<MultipartFile> documents = Arrays.asList(documentsArray);
	 * 
	 * dto.setDocuments(documents);
	 * 
	 * System.out.println("Due Date" + dto.getDueDate());
	 * 
	 * CorrespondenceLetter savedLetter = correspondenceService.saveLetter(dto);
	 * 
	 * return ResponseEntity.ok("Letter uploaded successfully: " +
	 * savedLetter.getLetterNumber()); } catch (Exception e) { e.printStackTrace();
	 * return ResponseEntity.badRequest().body("Failed to upload letter: " +
	 * e.getMessage()); } }
	 */
    
    @PostMapping(value = "/uploadLetter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLetter(
            @RequestPart("dto") String dtoJson,
            @RequestParam("document") MultipartFile[] documentsArray) {

        try {
            CorrespondenceUploadLetter dto =
                    objectMapper.readValue(dtoJson, CorrespondenceUploadLetter.class);

            dto.setDocuments(Arrays.asList(documentsArray));

            System.out.println("Due Date " + dto.getDueDate());

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
    public ResponseEntity<CorrespondenceLetterViewDto> getCorrespondenceWithFiles(@PathVariable Long id) {
        CorrespondenceLetterViewDto dto = correspondenceService.getCorrespondenceWithFiles(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/search")
    public ResponseEntity<List<CorrespondenceLetter>> filter(@RequestBody CorrespondenceLetter letter) {

        return ResponseEntity.ok(correspondenceService.getFiltered(letter));
    }

}
