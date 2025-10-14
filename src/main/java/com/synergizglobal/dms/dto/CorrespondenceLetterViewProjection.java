package com.synergizglobal.dms.dto;

public interface CorrespondenceLetterViewProjection {
    String getCategory();
    String getLetterNumber();
    java.time.LocalDate getLetterDate();
    
    String getSender();
    String getCopiedTo();
    String getCcRecipient();
    Integer getIsCc();  
    
    String getDepartment();	
    String getSubject();
    String getKeyInformation();
    String getRequiredResponse();
    java.time.LocalDate getDueDate();
    String getCurrentStatus();
    
    String getProjectName();
    String getContractName();

    String getOriginalRecipient();
    String getOriginalCcRecipient();
    
    String getFileName();
    String getFilePath();
    String getFileType();
    String getRefLetter();
}
