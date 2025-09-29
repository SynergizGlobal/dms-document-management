package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.SubFolder;

@Repository
public interface SubFolderRepository extends JpaRepository<SubFolder, Long> {
	
	List<SubFolder> findByFolderId(Long folderId);

	Optional<SubFolder> findByName(String subFolder);
	@Query(
		value= """
		select 
distinct sub.id,
sub.name,
f.id as folder_id
from dms.documents d 
left join dms.send_documents s on s.document_id = d.id
join dms.folders f on d.folder_id = f.id
join dms.sub_folders sub on f.id = sub.folder_id and d.sub_folder_id = sub.id
left join dms.documents_revision dr
	        ON dr.file_name = d.file_name AND dr.file_number = d.file_number
where
(d.created_by = :userId or (s.to_user_id = :userId AND s.status = 'Send') or (dr.created_by = :userId)) 
and d.not_required is null
and f.id = :folderId
and d.project_name in (:projects)
and d.contract_name in (:contracts)
			"""	
			, nativeQuery = true)
	List<SubFolder> getsubfolderGridByFolderId(@Param("folderId") Long folderId,@Param("userId") String userId,@Param("projects") List<String> projects,@Param("contracts") List<String>  contracts);
	@Query(
			value= """
			select 
	distinct sub.id,
	sub.name,
	f.id as folder_id
	from dms.documents d 
	join dms.folders f on d.folder_id = f.id
	join dms.sub_folders sub on f.id = sub.folder_id and d.sub_folder_id = sub.id
	where 
	d.not_required is null
	and f.id = :folderId
				"""	
				, nativeQuery = true)
	List<SubFolder> getAllSubfolderGridByFolderId(@Param("folderId") Long folderId,@Param("projects") List<String> projects,@Param("contracts") List<String>  contracts);
}
