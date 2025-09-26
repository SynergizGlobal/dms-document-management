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
    public List<CorrespondenceFolderFileDTO> getFiles(
            List<String> projectNames,
            List<String> contractNames,
            String type,
            String action,
            String baseUrl,
            String userId,
            boolean isAdmin) {

        return fileRepository.findFolderFilesByProjectsContractsAndType(
                projectNames, contractNames, type, action, baseUrl, userId, isAdmin
        );
    }

	@Override
	public List<CorrespondenceFolderFileDTO> getFilesForAdminIncoming(List<String> projectNames,
			List<String> contractNames, String type, String baseUrl) {
		// TODO Auto-generated method stub
		return fileRepository.getFilesForAdminIncoming(projectNames,
				contractNames, type, baseUrl);
	}

	@Override
	public List<CorrespondenceFolderFileDTO> getFilesForAdminOutgoing(List<String> projectNames,
			List<String> contractNames, String type, String baseUrl) {
		// TODO Auto-generated method stub
		return fileRepository.getFilesForAdminOutgoing(projectNames,
				 contractNames, type, baseUrl);
	}

	@Override
	public List<CorrespondenceFolderFileDTO> getFilesIncoming(List<String> projectNames, List<String> contractNames,
			String type, String baseUrl, String userId) {
		// TODO Auto-generated method stub
		return fileRepository.getFilesIncoming(projectNames, contractNames, type, baseUrl, userId);
	}

	@Override
	public List<CorrespondenceFolderFileDTO> getFilesOutgoing(List<String> projectNames, List<String> contractNames,
			String type, String baseUrl, String userId) {
		// TODO Auto-generated method stub
		return fileRepository.getFilesOutgoing(projectNames, contractNames, type, baseUrl, userId);
	}

}
