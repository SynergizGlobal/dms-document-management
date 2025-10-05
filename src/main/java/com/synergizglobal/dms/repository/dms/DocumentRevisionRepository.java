package com.synergizglobal.dms.repository.dms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.dto.DocumentRevisionDTO;
import com.synergizglobal.dms.entity.dms.DocumentRevision;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, Long> {

    @Query(value = "SELECT " +
                   "r.file_name AS fileName, " +
                   "r.file_number AS fileNumber, " +
                   "r.revision_no AS revisionNo, " +
                   "f.file_path AS filePath, " +
                   "f.file_type AS fileType " +
                   "FROM documents_revision r " +
                   "JOIN document_file f ON r.id = f.document_revision_id " +
                   "WHERE r.file_name = :fileName " +
                   "AND r.file_number = :fileNumber"
                   + " order by r.revision_no DESC", nativeQuery = true)
    List<DocumentRevisionDTO> findAllByFileNumberAndFileName(@Param("fileNumber") String fileNumber,
                                                              @Param("fileName") String fileName);
}