package com.synergizglobal.dms.service.dms;

import java.util.List;
import com.synergizglobal.dms.dto.FolderDTO;
import com.synergizglobal.dms.dto.SubFolderDTO;


public interface FolderService {


    public List<FolderDTO> getAllFolders();

    public FolderDTO getFolderById(Long id); 

    public FolderDTO createFolder(FolderDTO folderDTO);
    
    public FolderDTO updateFolder(Long id, FolderDTO folderDTO);
    
  
    
    public void deleteFolder(Long folderId);
    
    public void deleteSubFolder(Long subFolderId);

	public FolderDTO getFolderByName(String name);

	public List<FolderDTO> getAllFoldersByProjectsAndContracts(String project, String contract, String userId);
}
