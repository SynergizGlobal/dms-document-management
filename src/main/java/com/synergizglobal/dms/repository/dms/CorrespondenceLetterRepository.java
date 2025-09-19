package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewProjection;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;

@Repository
public interface CorrespondenceLetterRepository extends JpaRepository<CorrespondenceLetter, Long>{



    @Query(value = """
        select group_concat(distinct cf.file_type SEPARATOR '/') as fileType,
               c.correspondence_id  as correspondenceId,
               c.letter_number as letterNumber,
               c.category,
               c.recipient,
               c.subject,
               c.created_at as createdAt,
               c.updated_at as updatedAt,
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
        order by c.updated_at desc, c.created_at desc
        """, nativeQuery = true)
    List<CorrespondenceLetterProjection> findLetters(@Param("action") String action);
      
    @Query(value = """

			        SELECT
                                            	        c.category,
                                            	        c.letter_number AS letterNumber,
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
                                            	        f.file_type AS fileType,
                                                        rf.ref_letters AS refLetter
                                            	    FROM correspondence_letter c
                                            	    LEFT JOIN correspondence_file f\s
                                            	        ON c.correspondence_id = f.correspondence_id
                                        			LEFT JOIN correspondence_reference as cr
                                        				ON c.correspondence_id = cr.correspondence_letter_id
                                        			LEFT JOIN  reference_letter as rf\s
                                                        ON cr.reference_letter_id = rf.ref_id
                                            	    WHERE c.correspondence_id = :id
    	    """, nativeQuery = true)
    	List<CorrespondenceLetterViewProjection> findCorrespondenceWithFilesView(@Param("id") Long id);
    
    Optional<CorrespondenceLetter> findByLetterNumber(String letterNumber);



    @Query(value = """
    SELECT
        c.category,
        c.letter_number AS letterNumber,
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
        f.file_type AS fileType,
        rf.ref_letters AS refLetter
    FROM correspondence_letter c
    LEFT JOIN correspondence_file f
        ON c.correspondence_id = f.correspondence_id
    LEFT JOIN correspondence_reference cr
        ON c.correspondence_id = cr.correspondence_letter_id
    LEFT JOIN reference_letter rf
        ON cr.reference_letter_id = rf.ref_id
    WHERE c.letter_number = :letterNumber
    """, nativeQuery = true)
    List<CorrespondenceLetterViewProjection> findCorrespondenceWithFilesViewByLetterNumber(@Param("letterNumber") String letterNumber);


}

