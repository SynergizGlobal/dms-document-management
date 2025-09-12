package com.synergizglobal.dms.entity.dms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sub_folders")
public class SubFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    // Constructors
    public SubFolder() {}

    public SubFolder(String name, Folder folder) {
        this.name = name;
        this.folder = folder;
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
    
    public Folder getFolder() { 
    	return folder;
    }
    public void setFolder(Folder folder) {
    	this.folder = folder; 
    }
}

