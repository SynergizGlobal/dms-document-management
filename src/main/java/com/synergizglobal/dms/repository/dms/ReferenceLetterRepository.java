package com.synergizglobal.dms.repository.dms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.dms.ReferenceLetter;

public interface ReferenceLetterRepository extends JpaRepository<ReferenceLetter, Long> {

	List<ReferenceLetter>  findDistinctByRefLettersContainingIgnoreCase(String query);
	
}
