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
public class DocumentGridDTO {
	private String fileType;
    private String fileName;
    private String fileNumber;
    private String revisionNumber;
    private String revisionDate;
    private String projectName;
    private String contractName;
    private String folder;
    private String subFolder;
    private String department;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String documentType;
    private String viewedOrDownloaded;
    private String createdBy;
    private String dateUploaded;
}