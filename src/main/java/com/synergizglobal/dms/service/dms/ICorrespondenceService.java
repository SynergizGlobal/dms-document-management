package com.synergizglobal.dms.service.dms;

import java.util.List;

import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;

public interface ICorrespondenceService {


    CorrespondenceLetter saveLetter(CorrespondenceUploadLetter dto) throws Exception;

    List<CorrespondenceLetterProjection> getLettersByAction(String action);
    
    public List<String> findReferenceLetters(String query);
    
    public CorrespondenceLetterViewDto getCorrespondenceWithFiles(Long id);

    List<CorrespondenceLetter> getFiltered(CorrespondenceLetter letter);

    public CorrespondenceLetterViewDto getCorrespondenceWithFilesByLetterNumber(String letterNumber);
}
