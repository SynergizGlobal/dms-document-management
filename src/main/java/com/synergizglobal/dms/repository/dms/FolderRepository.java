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
		   select distinct f.*
		    from documents d
		    left join send_documents s on s.document_id = d.id
		    join folders f on f.id = d.folder_id
		    left join documents_revision dr
	        ON dr.file_name = d.file_name AND dr.file_number = d.file_number
			where
			 (d.created_by = :userId or (s.to_user_id = :userId AND s.status = 'Send') or (dr.created_by = :userId)) 
		      and (d.not_required is null or d.not_required = false)
		      and d.project_name in :projects
		      and d.contract_name in :contracts
		""", nativeQuery = true)
	List<Folder> getAllFoldersByProjectsAndContracts(@Param("projects") List<String> projects,@Param("contracts") List<String> contracts,@Param("userId") String userId);
	@Query("""
		    select distinct f
		    from Document d
		    join d.folder f
		    where 
		      (d.notRequired is null or d.notRequired = false)
		      and d.projectName in :projects
		      and d.contractName in :contracts
		""")
	List<Folder> getAllFoldersByProjectsAndContracts(@Param("projects")List<String> projects,@Param("contracts") List<String> contracts);
}
