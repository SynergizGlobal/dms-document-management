package com.synergizglobal.dms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrespondenceUploadLetter {
	private Long correspondenceId;
	private String category;
	private String letterCode;
	private String letterNumber;
	private String to;
	private List<String> cc;
	private List<String> referenceLetters;
	private String subject;
	private String keyInformation;
	private String requiredResponse;
	private Long currentStatus;
	private Long department;
	private List<MultipartFile> documents;
	private String action;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dueDate;
	private LocalDate letterDate;
	private String projectName;
	private String contractName;
	private List<String> removedExistingFiles;
	private Integer existingFilesCount;
}
