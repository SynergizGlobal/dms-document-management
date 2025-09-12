package com.synergizglobal.dms.dto;

import java.util.List;

public class FolderDTO {
    private Long id;
    private String name;
    private List<SubFolderDTO> subFolders;

    public FolderDTO() {}

    public FolderDTO(Long id, String name, List<SubFolderDTO> subFolders) {
        this.id = id;
        this.name = name;
        this.subFolders = subFolders;
    }

    // Getters & Setters
    public Long getId() { 
    	return id;
    }
    public void setId(Long id) { 
    	this.id = id;
    }

    public String getName() { 
    	return name;
    }
    public void setName(String name) {
    	this.name = name; 
    }

    public List<SubFolderDTO> getSubFolders() {
    	return subFolders; 
    }
    public void setSubFolders(List<SubFolderDTO> subFolders) { 
    	this.subFolders = subFolders; 
    }
}

