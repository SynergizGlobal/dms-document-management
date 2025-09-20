package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotRequiredDTO {
private Long documentId;
private String folder;
private String subFolder;
}
