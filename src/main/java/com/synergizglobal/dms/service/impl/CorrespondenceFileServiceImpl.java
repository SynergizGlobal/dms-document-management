package com.synergizglobal.dms.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO;
import com.synergizglobal.dms.repository.dms.CorrespondenceFileRepository;
import com.synergizglobal.dms.service.dms.CorrespondenceFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CorrespondenceFileServiceImpl implements CorrespondenceFileService{
	
    private final CorrespondenceFileRepository fileRepository;

	@Override
	public List<CorrespondenceFolderFileDTO> getFiles(List<String> projectNames, List<String> contractNames,
			String type, String baseUrl) {
	
		  return fileRepository.findFolderFilesByProjectsContractsAndType(projectNames, contractNames, type, baseUrl);
    }

}
