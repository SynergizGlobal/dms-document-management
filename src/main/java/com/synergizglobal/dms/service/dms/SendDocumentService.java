package com.synergizglobal.dms.service.dms;

import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.dto.DraftSendDocumentDTO;

public interface SendDocumentService {
	public DraftDataTableResponse<DraftSendDocumentDTO> getDrafts(DraftDataTableRequest request, String userId);
}
