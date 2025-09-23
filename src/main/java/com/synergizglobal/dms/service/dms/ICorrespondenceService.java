package com.synergizglobal.dms.service.dms;

import java.util.List;
import java.util.Map;

import com.synergizglobal.dms.dto.CorrespondenceGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.pmis.User;

public interface ICorrespondenceService {


    CorrespondenceLetter saveLetter(CorrespondenceUploadLetter dto, String baseUrl, String loggedUserId, String loggedUserName,String userRole) throws Exception;

    List<CorrespondenceLetterProjection> getLettersByAction(String action);

    public List<String> findReferenceLetters(String query);

    public CorrespondenceLetterViewDto getCorrespondenceWithFiles(Long id);

    List<CorrespondenceLetter> getFiltered(CorrespondenceLetter letter);

    public CorrespondenceLetterViewDto getCorrespondenceWithFilesByLetterNumber(String letterNumber);
    List<Map<String, Object>> fetchDynamic(List<String> fields, boolean distinct);

    List<CorrespondenceLetter> search(CorrespondenceLetter letter);
	List<CorrespondenceGridDTO> getFilteredCorrespondence(Map<Integer, List<String>> columnFilters, int start,
			int length, User user);

	long countFilteredCorrespondence(Map<Integer, List<String>> columnFilters, User user);

	long countAllCorrespondence(User user);

	DraftDataTableResponse<CorrespondenceGridDTO> getDrafts(DraftDataTableRequest request, String userId);
}
