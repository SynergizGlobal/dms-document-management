package com.synergizglobal.dms.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendDocumentDTO {
	private String sendTo;
	private String sendToUserId;
	private String sendCc;
	private String sendCcUserId;
	private String sendSubject;
	private String sendReason;
	private String responseExpected;
	private LocalDate targetResponseDate;
	private String attachmentName;
	private Long docId;
	private String status;
	private Long id;
}
