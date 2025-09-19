package com.synergizglobal.dms.repository.dms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.SendDocument;

@Repository
public interface SendDocumentRepository extends JpaRepository<SendDocument, Long>{

}
