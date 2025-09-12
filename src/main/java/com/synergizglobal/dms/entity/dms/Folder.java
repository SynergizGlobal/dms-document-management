package com.synergizglobal.dms.entity.dms;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubFolder> subFolders = new ArrayList<>();

    // Constructors
    public Folder() {}

    public Folder(String name) {
        this.name = name;
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

    public List<SubFolder> getSubFolders() { 
    	return subFolders; 
    }
    public void setSubFolders(List<SubFolder> subFolders) { 
    	this.subFolders = subFolders; 
    }
 }

