package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synergizglobal.dms.entity.dms.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	Optional<Folder> findByName(String folder);
	
	@Query("""
		    select distinct f
		    from Document d
		    left join d.sendDocument s
		    join d.folder f
		    where 
		      (d.createdBy = :userId or s.sendToUserId = :userId) 
		      and (d.notRequired is null or d.notRequired = false)
		      and d.projectName in :projects
		      and d.contractName in :contracts
		""")
	List<Folder> getAllFoldersByProjectsAndContracts(@Param("projects") List<String> projects,@Param("contracts") List<String> contracts,@Param("userId") String userId);
	

}
