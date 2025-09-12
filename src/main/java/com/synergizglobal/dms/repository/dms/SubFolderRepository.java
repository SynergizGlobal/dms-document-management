package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.SubFolder;

@Repository
public interface SubFolderRepository extends JpaRepository<SubFolder, Long> {
	
	List<SubFolder> findByFolderId(Long folderId);

	Optional<SubFolder> findByName(String subFolder);
}
