package com.synergizglobal.dms.entity.dms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "send_documents")
public class SendDocument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "send_to", nullable = false)
	private String sendTo;

	@Column(name = "to_user_id", nullable = false)
	private String sendToUserId;

	@Column(name = "send_cc")
	private String sendCc;

	@Column(name = "cc_user_id")
	private String sendCcUserId;

	@Column(name = "subject")
	private String sendSubject;

	@Column(name = "reason")
	private String sendReason;

	@Column(name = "response_expected")
	private String responseExpected;

	@Column(name = "target_response_date")
	private LocalDate targetResponseDate;

	@Column(name = "attachment")
	private String attachmentName;

	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(name = "status")
	private String status;
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@CreationTimestamp
	@Column(name = "updated_at", updatable = true)
	private LocalDateTime updatedAt;
}
