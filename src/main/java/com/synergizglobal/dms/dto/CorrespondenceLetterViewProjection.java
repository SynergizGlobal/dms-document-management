package com.synergizglobal.dms.dto;

public interface CorrespondenceLetterViewProjection {
    String getCategory();
    String getLetterNumber();
    java.time.LocalDate getLetterDate();
    String getSender();
    String getCopiedTo();
    String getCcRecipient();
    String getDepartment();
    String getSubject();
    String getKeyInformation();
    String getRequiredResponse();
    java.time.LocalDate getDueDate();
    String getCurrentStatus();

    String getFileName();
    String getFilePath();
    String getFileType();
}
