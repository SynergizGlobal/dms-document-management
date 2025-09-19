package com.synergizglobal.dms.repository.dms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.SendDocument;

@Repository
public interface SendDocumentRepository extends JpaRepository<SendDocument, Long>{
	Page<SendDocument> findByCreatedByAndStatus(String createdBy, String status, Pageable pageable);

	long countByCreatedByAndStatus(String createdBy, String status);
}
