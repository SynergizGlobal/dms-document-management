package com.synergizglobal.dms.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.FolderDTO;
import com.synergizglobal.dms.dto.SubFolderDTO;
import com.synergizglobal.dms.entity.dms.Folder;
import com.synergizglobal.dms.entity.dms.SubFolder;
import com.synergizglobal.dms.repository.dms.FolderRepository;
import com.synergizglobal.dms.repository.dms.SubFolderRepository;
import com.synergizglobal.dms.service.dms.FolderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl  implements FolderService{
	
	private final FolderRepository folderRepository;

	private final SubFolderRepository subFolderRepository;
    


	@Override
	public List<FolderDTO> getAllFolders() {
        return folderRepository.findAll().stream().map(folder -> new FolderDTO(
                folder.getId(),
                folder.getName(),
                folder.getSubFolders().stream()
                        .map(sf -> new SubFolderDTO(sf.getId(), sf.getName()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

	@Override
	public FolderDTO getFolderById(Long id) {
		 Folder folder = folderRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Folder not found"));
	        return new FolderDTO(
	                folder.getId(),
	                folder.getName(),
	                folder.getSubFolders().stream()
	                        .map(sf -> new SubFolderDTO(sf.getId(), sf.getName()))
	                        .collect(Collectors.toList())
	        );
	    }
	@Override
	public FolderDTO createFolder(FolderDTO folderDTO) {
        Folder folder = new Folder(folderDTO.getName());
        if (folderDTO.getSubFolders() != null) {
            folderDTO.getSubFolders().forEach(sf ->
                    folder.getSubFolders().add(new SubFolder(sf.getName(), folder)));
        }
        Folder saved = folderRepository.save(folder);
        return getFolderById(saved.getId());
    }

	@Override
	public FolderDTO updateFolder(Long id, FolderDTO folderDTO) {
	    Folder folder = folderRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Folder not found"));

	    // Update folder name
	    folder.setName(folderDTO.getName());

	    if (folderDTO.getSubFolders() != null) {
	        // Existing children in DB
	        Map<Long, SubFolder> existingSubFolders = folder.getSubFolders().stream()
	                .collect(Collectors.toMap(SubFolder::getId, sf -> sf));

	        // Iterate DTO subfolders
	        for (SubFolderDTO sfDto : folderDTO.getSubFolders()) {
	            if (sfDto.getId() != null && existingSubFolders.containsKey(sfDto.getId())) {
	                // Update existing subfolder
	                SubFolder existing = existingSubFolders.get(sfDto.getId());
	                existing.setName(sfDto.getName());
	                existingSubFolders.remove(sfDto.getId()); // Mark as processed
	            } else {
	                // New subfolder → add
	                folder.getSubFolders().add(new SubFolder(sfDto.getName(), folder));
	            }
	        }

	        // Optional: remove subfolders not present in DTO
	        // folder.getSubFolders().removeAll(existingSubFolders.values());
	    }

	    Folder updated = folderRepository.save(folder);
	    return getFolderById(updated.getId());
	}

	@Override
	public void deleteFolder(Long folderId) {
        folderRepository.deleteById(folderId); 
    }

	@Override
    public void deleteSubFolder(Long subFolderId) {
    	subFolderRepository.deleteById(subFolderId);
    }
}
