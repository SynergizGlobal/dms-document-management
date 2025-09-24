package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

	Page<CorrespondenceLetter> findByUserIdAndAction(String userId, String saveAsDraft, PageRequest pageRequest);

	Long countByUserIdAndAction(String userId, String saveAsDraft);


    @Query(value = "select count(*) from dms.correspondence_letter", nativeQuery = true)
    long countAllFiles();

    @Query(value = """
    	    select count(distinct c.correspondence_id)
    	    from dms.correspondence_letter c
    	    where c.user_name = :userId or c.to_user_id = :userId
    	    """, nativeQuery = true)
    long countAllFiles(String userId);


    @Query("select distinct c.category from CorrespondenceLetter c")
    List<String> findAllCategory();

    @Query("select distinct c.category from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedCategory(@Param("userId") String userId);

    @Query("select distinct c.letterNumber from CorrespondenceLetter c")
    List<String> findAllLetterNumbers();


    @Query("select distinct c.letterNumber from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedLetterNumbers(@Param("userId") String userId);

    @Query("select distinct c.userName from CorrespondenceLetter c")
    List<String> findAllFrom();

    @Query("select distinct c.userName from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedFrom(@Param("userId") String userId);

    @Query("select distinct c.subject from CorrespondenceLetter c")
    List<String> findAllSubject();

    @Query("select distinct c.subject from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedSubject(@Param("userId") String userId);

    @Query("select distinct c.requiredResponse from CorrespondenceLetter c")
    List<String> findAllRequiredResponse();

    @Query("select distinct c.requiredResponse from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedRequiredResponse(@Param("userId") String userId);

    @Query("select distinct c.dueDate from CorrespondenceLetter c")
    List<String> findAllDueDates();

    @Query("select distinct c.dueDate from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedDueDates(@Param("userId") String userId);

    @Query("select distinct c.projectName from CorrespondenceLetter c")
    List<String> findAllProjectNames();

    @Query("select distinct c.projectName from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedProjectNames(@Param("userId") String userId);

    @Query("select distinct c.contractName from CorrespondenceLetter c")
    List<String> findAllContractNames();

    @Query("select distinct c.contractName from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedContractNames(@Param("userId") String userId);

    @Query("select distinct c.currentStatus from CorrespondenceLetter c")
    List<String> findAllStatus();

    @Query("select distinct c.currentStatus from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedStatus(@Param("userId") String userId);
    @Query("select distinct c.department from CorrespondenceLetter c")
    List<String> findAllDepartment();

    @Query("select distinct c.department from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedDepartment(@Param("userId") String userId);

    @Query("select distinct c.fileCount from CorrespondenceLetter c")
    List<String> findAllAttachment();

    @Query("select distinct c.fileCount from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedAttachment(@Param("userId") String userId);

    @Query("select distinct c.mailDirection from CorrespondenceLetter c")
    List<String> findAllTypesOfMail();

    @Query("select distinct c.mailDirection from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedTypesOfMail(@Param("userId") String userId);

    @Query("select distinct c.to from CorrespondenceLetter c")
    List<String> findAllToSend();

    @Query("select distinct c.to from CorrespondenceLetter c where c.userId = :userId")
    List<String> findGroupedToSend(@Param("userId") String userId);
}

