package com.synergizglobal.dms.repository.dms;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewProjection;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;

@Repository
public interface CorrespondenceLetterRepository extends JpaRepository<CorrespondenceLetter, Long>{



    @Query(value = """
        select group_concat(distinct cf.file_type SEPARATOR '/') as fileType,
               c.correspondence_id  as correspondenceId,
               c.category,
               c.recipient,
               c.subject,
               c.required_response as requiredResponse,
               c.due_date as dueDate,
               c.current_status as currentStatus,
               c.department,
               c.file_count as fileCount,
               c.action
        from correspondence_letter c
        join correspondence_file cf on c.correspondence_id = cf.correspondence_id
        where c.action = :action
        group by c.correspondence_id
        """, nativeQuery = true)
    List<CorrespondenceLetterProjection> findLetters(@Param("action") String action);
    
    
//    @Query("SELECT new com.synergizglobal.dms.dto.CorrespondenceLetterViewDto(
//            c.category, c.letterName, c.letterDate, c.to,
//            c.ccRecipient, c.department, 
//            c.subject, c.keyInformation, c.requiredResponse, 
//            c.dueDate, c.currentStatus
//         )
//         FROM CorrespondenceLetter c
//         WHERE c.correspondenceId = :id")
//CorrespondenceLetterViewDto findCorrespondenceById(@Param("id") Long id);
    
    @Query(value = """
    	    SELECT 
    	        c.category,
    	        c.letter_name AS letterNumber,
    	        c.letter_date AS letterDate,
    	        c.recipient AS sender,
    	        c.recipient AS copiedTo,
    	        c.cc_recipient AS ccRecipient,
    	        c.department,
    	        c.subject,
    	        c.key_information AS keyInformation,
    	        c.required_response AS requiredResponse,
    	        c.due_date AS dueDate,
    	        c.current_status AS currentStatus,
    	        f.file_name AS fileName,
    	        f.file_path AS filePath,
    	        f.file_type AS fileType
    	    FROM correspondence_letter c
    	    LEFT JOIN correspondence_file f 
    	        ON c.correspondence_id = f.correspondence_id
    	    WHERE c.correspondence_id = :id
    	    """, nativeQuery = true)
    	List<CorrespondenceLetterViewProjection> findCorrespondenceWithFilesView(@Param("id") Long id);

}

