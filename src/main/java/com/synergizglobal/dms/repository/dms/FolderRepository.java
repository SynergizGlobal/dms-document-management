package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synergizglobal.dms.entity.dms.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	Optional<Folder> findByName(String folder);
	
	@Query(value="""
			select
distinct f.id,
f.name
from dms.documents d 
left join dms.send_documents s on s.document_id = d.id
join dms.folders f on d.folder_id = f.id
where 
(d.created_by = :userId or s.to_user_id = :userId) 
and d.not_required is null
and d.project_name in (:project)
and d.contract_name in (:contract)
			""", nativeQuery = true)
	List<Folder> getAllFoldersByProjectsAndContracts(@Param("project") String project,@Param("contract") String contract,@Param("userId") String userId);
	

}
