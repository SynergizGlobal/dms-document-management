package com.synergizglobal.dms.entity.dms;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "metadata")
public class MetaData {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_number", nullable = false)
    private String fileNumber;

    @Column(name = "revision_no", nullable = false)
    private String revisionNo;

    @Column(name = "revision_date")
    private LocalDate revisionDate;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "contract_name")
    private String contractName;
    
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "sub_folder_id")
    private SubFolder subFolder;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status currentStatus;
	  
    @Column(name = "upload_document")
    private String uploadDocument;  
	
    @Column(name = "upload_zip_location")
    private String uploadedZipLocation;    
    
    @ManyToOne
    @JoinColumn(name = "uploaded_metadata_id")
    private UploadedMetaData uploadedMetaData;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

	
}    