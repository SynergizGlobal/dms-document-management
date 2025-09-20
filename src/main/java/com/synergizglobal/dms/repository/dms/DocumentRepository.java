package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synergizglobal.dms.dto.DocumentFolderGridDTO;
import com.synergizglobal.dms.entity.dms.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	Optional<Document> findByFileNumber(String fileNumber);

	Optional<Document> findByFileNameAndFileNumber(String fileName, String fileNumber);
	
	@EntityGraph(attributePaths = "documentFiles")
	Optional<Document> findByFileName(String fileName);
	
	@Query("SELECT distinct d.fileName FROM Document d GROUP BY d.fileName")
    List<String> findGroupedFileNames();
	
	@Query("SELECT DISTINCT df.fileType FROM Document d JOIN d.documentFiles df GROUP BY df.fileType")
	List<String> findGroupedFileTypes();
	
	@Query("SELECT distinct d.fileNumber FROM Document d GROUP BY d.fileNumber")
	List<String> findGroupedFileNumbers();
	
	@Query("SELECT distinct d.revisionNo FROM Document d GROUP BY d.revisionNo")
	List<String> findGroupedRevisionNos();
	
	@Query("SELECT DISTINCT ds.name FROM Document d JOIN d.currentStatus ds GROUP BY d.currentStatus")
	List<String> findGroupedStatus();
	
	@Query("SELECT DISTINCT ds.name FROM Document d JOIN d.folder ds GROUP BY d.folder")
	List<String> findGroupedFolders();
	
	@Query("SELECT DISTINCT ds.name FROM Document d JOIN d.subFolder ds GROUP BY d.subFolder")
	List<String> findGroupedSubFolders();

	@Query("SELECT distinct DATE_FORMAT(d.createdAt, '%Y-%m-%d') FROM Document d GROUP BY DATE_FORMAT(d.createdAt, '%Y-%m-%d')")
	List<String> findGroupedUploadedDate();
	
	@Query("SELECT distinct DATE_FORMAT(d.revisionDate, '%Y-%m-%d') FROM Document d GROUP BY d.revisionDate")
	List<String> findGroupedRevisionDate();

	@Query("SELECT DISTINCT ds.name FROM Document d JOIN d.department ds GROUP BY d.department")
	List<String> findGroupedDepartment();
	
	@Query("SELECT COUNT(df.id) FROM Document d JOIN d.documentFiles df")
	long countAllFiles();

	@Query("""
		    SELECT COUNT(DISTINCT df.id)
		    FROM Document d
		    JOIN d.documentFiles df
		    LEFT JOIN d.sendDocument sd
		    WHERE (d.createdBy = :userId
		           OR sd.sendToUserId = :userId)
		    """)
	long countAllFiles(@Param("userId") String userId);
	
	@Query("SELECT distinct d.createdBy FROM Document d GROUP BY d.createdBy")
	List<String> findGroupedCreatedBy();

	@Query("SELECT distinct d.projectName FROM Document d GROUP BY d.projectName")
	List<String> findGroupedProjectNames();

	@Query("SELECT distinct d.contractName FROM Document d GROUP BY d.contractName")
	List<String> findGroupedContractNames();
	
	@Query(value = "select \r\n"
			+ "f.file_path\r\n"
			+ "from dms.document_file f\r\n"
			+ "join dms.documents d on f.document_id = d.id\r\n"
			+ "where d.file_name = :fileName\r\n"
			+ "and d.file_number = :fileNumber\r\n"
			+ "and d.revision_no = :revisionNo", nativeQuery = true)
	String getFilePath(@Param("fileName")String fileName, @Param("fileNumber")String fileNumber,@Param("revisionNo") String revisionNo);

	@Query(
			value ="""
			select
distinct d.file_name as fileName,
files.file_path as filePath,
files.file_type as fileType
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id
join dms.sub_folders sub on d.sub_folder_id = sub.id
where
(s.to_user_id = :userId or s.to_user_id is null)
and d.created_by = :userId
and sub.id = :subfolderId
			"""
			, nativeQuery = true)
	List<DocumentFolderGridDTO> getFilesForFolderGrid(@Param("subfolderId") String subfolderId,@Param("userId") String userId);
}