package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileViewDto {
    private String fileName;
    private String fileType;
    private String filePath;
    private String downloadUrl;
}