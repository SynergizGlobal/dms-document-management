package com.synergizglobal.dms.dto;

import com.synergizglobal.dms.entity.pmis.User;

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
}
