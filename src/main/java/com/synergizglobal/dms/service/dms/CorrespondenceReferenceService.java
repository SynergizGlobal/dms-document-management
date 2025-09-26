package com.synergizglobal.dms.service.dms;

import java.util.List;

import com.synergizglobal.dms.dto.ReferenceLetterDTO;

public interface CorrespondenceReferenceService {
	 
	 List<ReferenceLetterDTO> getReferenceLettersByCorrespondenceId(Long correspondenceId);
	 
	 
 }