package com.synergizglobal.dms.service.impl;

import com.synergizglobal.dms.dto.ReferenceLetterDTO;
import com.synergizglobal.dms.repository.dms.CorrespondenceReferenceRepository;
import com.synergizglobal.dms.service.dms.CorrespondenceReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CorrespondenceReferenceServiceIMPL implements CorrespondenceReferenceService {

    private final CorrespondenceReferenceRepository correspondenceReferenceRepository;


    @Override
    public List<ReferenceLetterDTO> getReferenceLettersByCorrespondenceId(Long correspondenceId) {
        return correspondenceReferenceRepository.findReferenceLettersByCorrespondenceId(correspondenceId)
                .stream()
                .map(ref -> new ReferenceLetterDTO(ref.getRefId(), ref.getRefLetters()))
                .toList();
    }
}
