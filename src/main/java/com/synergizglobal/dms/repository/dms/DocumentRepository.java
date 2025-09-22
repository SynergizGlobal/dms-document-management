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
	
	
	@Query(value="""
			select
distinct d.file_name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
    List<String> findGroupedFileNames(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct files.file_type
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedFileTypes(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct d.file_number
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedFileNumbers(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct d.revision_no
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedRevisionNos(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct st.name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
left join dms.statuses st on st.id = d.status_id
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedStatus(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct f.name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
left join dms.folders f on f.id = d.folder_id
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedFolders(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct sub.name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
left join dms.sub_folders sub on sub.id = d.sub_folder_id
where
(s.to_user_id = :userId or s.to_user_id is null)
and d.created_by = :userId
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedSubFolders(@Param("userId") String userId);

	@Query(value="""
			select
distinct DATE_FORMAT(d.created_at, '%Y-%m-%d')
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedUploadedDate(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct DATE_FORMAT(d.revision_date, '%Y-%m-%d')
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedRevisionDate(@Param("userId") String userId);

	@Query(value="""
			select
distinct dpt.name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
left join dms.departments dpt on dpt.id = d.department_id
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedDepartment(@Param("userId") String userId);
	
	@Query(value ="""
			   select
	count(distinct d.id)
	from dms.documents d
	left join dms.document_file files on files.document_id = d.id 
	left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
	where
	 d.not_required is null
			    """, nativeQuery = true)
	long countAllFiles();

	@Query(value ="""
		   select
count(distinct d.id)
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
left join dms.departments dpt on dpt.id = d.department_id
where
(d.created_by = :userId or s.to_user_id = :userId) 
and files.file_type is not null
and d.not_required is null
		    """, nativeQuery = true)
	long countAllFiles(@Param("userId") String userId);
	
	@Query(value="""
			select
distinct d.created_by
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedCreatedBy(@Param("userId") String userId);

	@Query(value="""
			select
distinct d.project_name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedProjectNames(@Param("userId") String userId);

	@Query(value="""
			select
distinct d.contract_name
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id and s.status = 'Send'
where
(d.created_by = :userId or s.to_user_id = :userId)
and files.file_type is not null
and d.not_required is null
			""", nativeQuery = true)
	List<String> findGroupedContractNames(@Param("userId") String userId);
	
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
files.file_type as fileType,
d.revision_no as revisionNo
from dms.documents d
left join dms.document_file files on files.document_id = d.id 
left join dms.send_documents s on s.document_id = d.id
join dms.sub_folders sub on d.sub_folder_id = sub.id
where
(d.created_by = :userId or s.to_user_id = :userId)
and d.not_required is null
and sub.id = :subfolderId
			"""
			, nativeQuery = true)
	List<DocumentFolderGridDTO> getFilesForFolderGrid(@Param("subfolderId") String subfolderId,@Param("userId") String userId);

	@Query(
			value ="""
			select 
distinct d.file_name as fileName,
f.file_path as filePath,
f.file_type as fileType,
d.revision_no as revisionNo
from dms.documents d
left join dms.send_documents s on s.document_id = d.id
join dms.document_file f on f.document_id = d.id
join dms.sub_folders sub on sub.id = d.sub_folder_id
where d.not_required = 1
and (d.created_by = :userId or s.to_user_id = :userId)
and sub.id = :subfolderId
union
select 
distinct d.file_name as fileName,
f.file_path as filePath,
f.file_type as fileType,
d.revision_no as revisionNo
from dms.documents_revision d
join dms.document_file f on f.document_revision_id = d.id
join dms.sub_folders sub on sub.id = d.sub_folder_id
and (d.created_by = :userId)
and sub.id = :subfolderId
			"""
			, nativeQuery = true)
	List<DocumentFolderGridDTO> getArvhivedFilesForFolderGrid(@Param("subfolderId") String subfolderId,@Param("userId") String userId);
}