package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFolderGridDTO {
private String fileName;
private String filePath;
private String fileType;
private String revisionNo;
}
