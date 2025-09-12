package com.synergizglobal.dms.entity.dms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DocumentFile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileType;
    private String filePath;
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
    
    @ManyToOne
    @JoinColumn(name = "document_revision_id")
    private DocumentRevision documentRevision;
}

