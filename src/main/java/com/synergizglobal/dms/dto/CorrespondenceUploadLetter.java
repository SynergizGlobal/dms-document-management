package com.synergizglobal.dms.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CorrespondenceUploadLetter {
	
	
	private String category;
	private String letterName;
	private String to;
	private List<String> cc;
	private List<String> referenceLetters;
	private String subject;
	private String keyInformation;
	private String requiredInformation;
	private String requiredResponse;
	private String currentStatus;
	private String department;
	private List<MultipartFile> documents;
	private String action;
	
     
	
	
}
