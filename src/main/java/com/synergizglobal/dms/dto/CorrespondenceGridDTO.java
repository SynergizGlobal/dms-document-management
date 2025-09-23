package com.synergizglobal.dms.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrespondenceGridDTO {
	private Long correspondenceId;
	private String category;
	private String letterNumber;
	private String from;
	private String to;
	private String subject;
	private String requiredResponse;
	private String currentStatus;
	private String department;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dueDate;
	private String projectName;
	private String contractName;
	private Integer attachment;
	private String type;
}
