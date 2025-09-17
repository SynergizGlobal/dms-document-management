package com.synergizglobal.dms.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SaveMetaDataDto {
	private String filename;
	private String filenumber;
	private String revisionno;
	private LocalDate revisiondate;
	private String projectname;
	private String contractname;
	private Long folder;
	private Long subfolder;
	private Long department;
	private Long currentstatus;
	private String uploaddocument;
}
