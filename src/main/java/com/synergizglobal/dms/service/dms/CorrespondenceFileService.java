package com.synergizglobal.dms.service.dms;

import java.util.List;

import com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO;

public interface CorrespondenceFileService {
	
	
	 List<CorrespondenceFolderFileDTO> getFiles(List<String> projectNames, List<String> contractNames, String type, String baseUrl);

}
