package com.synergizglobal.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceLetterDTO {
    private Long refId;
    private String refLetters;
}
