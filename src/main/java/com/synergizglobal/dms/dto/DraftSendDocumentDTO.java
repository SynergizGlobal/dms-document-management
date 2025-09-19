package com.synergizglobal.dms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftSendDocumentDTO {
	private Long id;
	private String sendTo;
	private String sendToUserId;
	private String sendCc;
	private String sendCcUserId;
	private String sendSubject;
	private String sendReason;
	private String responseExpected;
	private String targetResponseDate;
	private String attachmentName;
	private String status;
	private String createdBy;
	private String createdAt;
	private Long docId;
	
	public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.createdAt = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
	
	public void setTargetResponseDate(LocalDate date) {
        if (date != null) {
            this.targetResponseDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}
