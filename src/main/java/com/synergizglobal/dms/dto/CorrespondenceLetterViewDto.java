package com.synergizglobal.dms.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrespondenceLetterViewDto {
    private String category;
    private String letterNumber;
    private LocalDate letterDate;
    private String sender;
    private String copiedTo;
    private String ccRecipient;
    private String department;
    private String subject;
    private String keyInformation;
    private String requiredResponse;
    private LocalDate dueDate;
    private String currentStatus;
    private List<FileViewDto> files;
    private List<String> refLetters;
}


