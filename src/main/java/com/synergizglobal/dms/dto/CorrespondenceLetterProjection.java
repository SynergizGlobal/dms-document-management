package com.synergizglobal.dms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CorrespondenceLetterProjection {
	Long   getCorrespondenceId();
	String getLetterNumber();
    String getFileType();
    String getCategory();
    String getRecipient();
    String getSubject();
    String getRequiredResponse();
    LocalDate getDueDate();
    String getCurrentStatus();
    String getDepartment();
    Integer getFileCount();
    String getAction();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
