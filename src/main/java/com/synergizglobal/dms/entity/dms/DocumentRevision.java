package com.synergizglobal.dms.entity.dms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.synergizglobal.dms.entity.pmis.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documents_revision")
public class DocumentRevision {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_number", nullable = false)
    private String fileNumber;

    @Column(name = "file_db_number", unique = true)
    private String fileDBNumber;
    
    @Column(name = "revision_no", nullable = false)
    private String revisionNo;

    @Column(name = "revision_date")
    private LocalDate revisionDate;
    
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

    @Column(name = "created_by")
    private String createdBy;
   
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "contract_name")
    private String contractName;
    
    @Column(name = "reason_for_update")
    private String reasonForUpdate;
    
	@OneToMany(mappedBy = "documentRevision", cascade = CascadeType.ALL, orphanRemoval = true) 
	private List<DocumentFile> documentFiles = new ArrayList<>();
	  
	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;
	    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

	
}    


