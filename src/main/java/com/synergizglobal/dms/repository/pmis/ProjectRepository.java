package com.synergizglobal.dms.repository.pmis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.pmis.Project;

@Repository
public interface ProjectRepository  extends JpaRepository<Project, String>{
	
}
