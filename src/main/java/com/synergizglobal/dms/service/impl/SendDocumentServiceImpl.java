package com.synergizglobal.dms.service.impl;

import java.util.List;

import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.dto.DraftSendDocumentDTO;
import com.synergizglobal.dms.entity.dms.SendDocument;
import com.synergizglobal.dms.repository.dms.SendDocumentRepository;
import com.synergizglobal.dms.service.dms.SendDocumentService;

@Service
public class SendDocumentServiceImpl implements SendDocumentService {
	
	@Autowired
	private SendDocumentRepository sendDocumentRepository;
	
	@Override
	public DraftDataTableResponse<DraftSendDocumentDTO> getDrafts(DraftDataTableRequest request, String userId) {
		int page = request.getStart() / request.getLength();
	    PageRequest pageRequest = PageRequest.of(page, request.getLength(), Sort.by(Sort.Direction.DESC, "updatedAt"));

	    org.springframework.data.domain.Page<SendDocument> resultPage = sendDocumentRepository.findByCreatedByAndStatus(userId, "Draft" ,pageRequest);
	    
	    List<DraftSendDocumentDTO> dtos = resultPage.getContent()
	            .stream()
	            .map(this::mapToDTO)
	            .toList();
	    
	    return new DraftDataTableResponse<>(
	        request.getDraw(),
	        sendDocumentRepository.countByCreatedByAndStatus(userId, "Draft"),
	        resultPage.getTotalElements(),
	        dtos
	    );
	}
	private DraftSendDocumentDTO mapToDTO(SendDocument entity) {
	    DraftSendDocumentDTO dto = new DraftSendDocumentDTO();
	    dto.setId(entity.getId());
	    dto.setSendTo(entity.getSendTo());
	    dto.setSendToUserId(entity.getSendToUserId());
	    dto.setSendCc(entity.getSendCc());
	    dto.setSendCcUserId(entity.getSendCcUserId());
	    dto.setSendSubject(entity.getSendSubject());
	    dto.setSendReason(entity.getSendReason());
	    dto.setResponseExpected(entity.getResponseExpected());
	    dto.setTargetResponseDate(entity.getTargetResponseDate());
	    dto.setAttachmentName(entity.getAttachmentName());
	    dto.setStatus(entity.getStatus());
	    dto.setCreatedBy(entity.getCreatedBy());
	    dto.setCreatedAt(entity.getCreatedAt());
	    dto.setDocId(entity.getDocument().getId());
	    return dto;
	}

}
