package com.synergizglobal.dms.repository.dms;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.dms.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	Optional<Document> findByFileNumber(String fileNumber);

	Optional<Document> findByFileNameAndFileNumber(String fileName, String fileNumber);
	
	@EntityGraph(attributePaths = "documentFiles")
	Optional<Document> findByFileName(String fileName);

}