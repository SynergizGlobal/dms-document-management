package com.synergizglobal.dms.entity.dms;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Entity
@Builder
@Table(name = "documents")
public class Document {

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

   
	  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval =
	  true, fetch = FetchType.EAGER) 
	  private List<DocumentFile> documentFiles;
	  
	  
	  
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

	
}    


