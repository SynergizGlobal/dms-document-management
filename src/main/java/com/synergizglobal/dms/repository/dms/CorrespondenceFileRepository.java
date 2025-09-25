package com.synergizglobal.dms.repository.dms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO;
import com.synergizglobal.dms.entity.dms.CorrespondenceFile;

public interface CorrespondenceFileRepository extends JpaRepository<CorrespondenceFile, Long> {

	 @Query("""
		        SELECT new com.synergizglobal.dms.dto.CorrespondenceFolderFileDTO(
		            f.fileName, 
		            f.fileType, 
		            f.filePath, 
		            CONCAT(:baseUrl, '/files/', f.filePath), 
		            c.letterNumber, 
		            s.fromDept, 
		            s.toDept, 
		            s.type
		        )
		        FROM CorrespondenceFile f
		        JOIN f.correspondenceLetter c
		        JOIN c.sendCorLetters s
		        WHERE c.projectName IN :projectNames
		          AND c.contractName IN :contractNames
		          AND s.type = :type
		          AND s.isCC = false
		          AND c.action = :action
		          AND (:isAdmin = true OR s.fromUserId = :userId OR s.toUserId = :userId)
		    """)
		    List<CorrespondenceFolderFileDTO> findFolderFilesByProjectsContractsAndType(
		            @Param("projectNames") List<String> projectNames,
		            @Param("contractNames") List<String> contractNames,
		            @Param("type") String type,
		            @Param("action") String action,
		            @Param("baseUrl") String baseUrl,
		            @Param("userId") String userId,
		            @Param("isAdmin") boolean isAdmin
		    );
}
