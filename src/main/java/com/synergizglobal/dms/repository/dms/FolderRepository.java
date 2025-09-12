package com.synergizglobal.dms.repository.dms;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.dms.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	Optional<Folder> findByName(String folder);
	

}
