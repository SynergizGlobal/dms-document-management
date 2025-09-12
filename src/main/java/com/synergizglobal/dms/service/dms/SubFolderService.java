package com.synergizglobal.dms.service.dms;

import java.util.List;

import com.synergizglobal.dms.dto.FolderDTO;
import com.synergizglobal.dms.entity.dms.SubFolder;


public interface SubFolderService {

	List<SubFolder> getSubFoldersByFolderId(Long folderId);

}
