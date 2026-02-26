package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondenceFolderFileDTO {
	private Long correspondenceId; 
    private String fileName;
    private String fileType;
    private String filePath;
    private String downloadUrl;
    private String letterCode;
    private String letterNumber;
    private String fromDept;
    private String toDept;
    private String type;
}
