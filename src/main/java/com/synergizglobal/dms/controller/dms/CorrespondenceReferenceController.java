package com.synergizglobal.dms.controller.dms;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.ReferenceLetterDTO;
import com.synergizglobal.dms.service.dms.CorrespondenceReferenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/correspondence")
@RequiredArgsConstructor
public class CorrespondenceReferenceController {

    private final  CorrespondenceReferenceService correspondenceReferenceService;

    @GetMapping("/references/{correspondenceId}")
    public ResponseEntity<List<ReferenceLetterDTO>> getReferenceLetters(
            @PathVariable("correspondenceId") Long correspondenceId) {
        List<ReferenceLetterDTO> referenceLetters =
                correspondenceReferenceService.getReferenceLettersByCorrespondenceId(correspondenceId);
        return ResponseEntity.ok(referenceLetters);
    }
}

 
