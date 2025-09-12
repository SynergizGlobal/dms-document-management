package com.synergizglobal.dms.dto;

import java.time.LocalDate;

public interface CorrespondenceLetterProjection {
	Long   getCorrespondenceId();
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
}
