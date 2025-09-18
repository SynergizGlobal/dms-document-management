package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRevisionDTO {
	private String fileName;
	private String fileNumber;
	private String RevisionNo;
	private String filePath;
	private String fileType;
}
