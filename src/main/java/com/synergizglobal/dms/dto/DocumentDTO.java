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
public class DocumentDTO {
	private String errorMessage;
    private String fileName;
    private String fileNumber;
    private String revisionNo;
    private LocalDate revisionDate;
    private String projectName;
    private String contractName;
    private String folder;
    private String subFolder;
    private String department;
    private String currentStatus;
}