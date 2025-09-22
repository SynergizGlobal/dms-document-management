package com.synergizglobal.dms.service.dms;

import java.util.List;

import com.synergizglobal.dms.entity.dms.SubFolder;


public interface SubFolderService {

	List<SubFolder> getSubFoldersByFolderId(Long folderId);

	List<SubFolder> getsubfolderGridByFolderId(Long folderId, String userId, List<String> projects, List<String> contracts);

	List<SubFolder> getAllSubfolderGridByFolderId(Long folderId, List<String> projects, List<String> contracts);

}
