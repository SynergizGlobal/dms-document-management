package com.synergizglobal.dms.repository.dms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.dto.CorrespondenceDraftGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewProjection;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;

import jakarta.persistence.SqlResultSetMapping;

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
                                            	        c.department,
                                            	        c.subject,
                                            	        c.key_information AS keyInformation,
                                            	        c.required_response AS requiredResponse,
                                            	        c.due_date AS dueDate,
                                            	        c.current_status AS currentStatus,
                                            	        f.file_name AS fileName,
                                            	        f.file_path AS filePath,
                                            	        f.file_type AS fileType,
                                                        rf.ref_letters AS refLetter,
                                                          sc.from_user_name      AS sender,
                                                        CASE WHEN sc.is_cc = 0 OR sc.is_cc IS NULL THEN sc.to_user_name END AS copiedTo,
                                                        CASE WHEN sc.is_cc = 1 THEN sc.to_user_name END AS ccRecipient,
                                                           sc.is_cc       AS isCc
                                            	    FROM correspondence_letter c
                                            	    LEFT JOIN correspondence_file f\s
                                            	        ON c.correspondence_id = f.correspondence_id
                                        			LEFT JOIN correspondence_reference as cr
                                        				ON c.correspondence_id = cr.correspondence_letter_id
                                        			LEFT JOIN  reference_letter as rf\s
                                                        ON cr.reference_letter_id = rf.ref_id
                                                        LEFT JOIN send_correspondence_letter sc
                                                      ON c.correspondence_id = sc.correspondence_id   
                                            	    WHERE c.correspondence_id = :id
    	    """, nativeQuery = true)
    	List<CorrespondenceLetterViewProjection> findCorrespondenceWithFilesView(@Param("id") Long id);
    
    Optional<CorrespondenceLetter> findByLetterNumber(String letterNumber);
    



    @Query(value = """
    SELECT
        c.category,
        c.letter_number AS letterNumber,
        c.letter_date AS letterDate,
        c.department,
        c.subject,
        c.key_information AS keyInformation,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.current_status AS currentStatus,
        f.file_name AS fileName,
        f.file_path AS filePath,
        f.file_type AS fileType,
        rf.ref_letters AS refLetter,
        sc.from_user_name      AS sender,
        CASE WHEN sc.is_cc = 0 OR sc.is_cc IS NULL THEN sc.to_user_name END AS copiedTo,
    CASE WHEN sc.is_cc = 1 THEN sc.to_user_name END AS ccRecipient,
    sc.is_cc       AS isCc
    FROM correspondence_letter c
    LEFT JOIN correspondence_file f
        ON c.correspondence_id = f.correspondence_id
    LEFT JOIN correspondence_reference cr
        ON c.correspondence_id = cr.correspondence_letter_id
    LEFT JOIN reference_letter rf
        ON cr.reference_letter_id = rf.ref_id
    LEFT JOIN send_correspondence_letter sc
        ON c.correspondence_id = sc.correspondence_id
    WHERE c.letter_number = :letterNumber
    """, nativeQuery = true)
    List<CorrespondenceLetterViewProjection> findCorrespondenceWithFilesViewByLetterNumber(@Param("letterNumber") String letterNumber);

    @Query(value ="""
    		SELECT DISTINCT
        sl.correspondence_id AS correspondencId,
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        DATE_FORMAT(c.due_date, '%Y-%m-%d') as dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'Save as Draft'
      AND sl.from_user_id = :userId
    ORDER BY c.UPDATED_AT DESC
    LIMIT :limit OFFSET :offset
    		"""
    		, nativeQuery = true
    		)
    List<CorrespondenceDraftGridDTO> findByUserIdAndAction(@Param("userId") String userId,@Param("limit") Integer limit,@Param("offset") Integer offset);

    @Query(value ="""
    		SELECT COUNT(*) from (
    SELECT 
        sl.correspondence_id AS correspondencId,
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        DATE_FORMAT(c.due_date, '%Y-%m-%d') as dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'Save as Draft'
      AND sl.from_user_id = :userId
    ORDER BY c.UPDATED_AT
    ) x
    		"""
    		, nativeQuery = true
    		)
	Long countByUserIdAndAction(@Param("userId") String userId,@Param("saveAsDraft") String saveAsDraft);


    @Query(value = " SELECT count(*) FROM (  \r\n"
    		+ "SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  \r\n"
    		+ "sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, \r\n"
    		+ " c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, \r\n"
    		+ " c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, \r\n"
    		+ " sl.type as type  FROM dms.correspondence_letter c  \r\n"
    		+ " LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id \r\n"
    		+ " AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' \r\n"
    		+ " GROUP BY c.correspondence_id  UNION \r\n"
    		+ " SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, \r\n"
    		+ " sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, \r\n"
    		+ " c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,\r\n"
    		+ " c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,\r\n"
    		+ " sl.type as type  FROM dms.correspondence_letter c \r\n"
    		+ " LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.is_cc = 0  \r\n"
    		+ " WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id \r\n"
    		+ " ) x WHERE 1=1  ORDER BY x.dueDate ", nativeQuery = true)
    long countAllFiles();

    @Query(value = """
    	    SELECT count(*) FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    long countAllFiles(@Param("userId") String userId);


    @Query(value="""
    		SELECT distinct category 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllCategory();

    @Query(value = """
    	    SELECT distinct category FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedCategory(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct letterNumber 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllLetterNumbers();


    @Query(value = """
    	    SELECT distinct letterNumber FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedLetterNumbers(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct `from` 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllFrom();

    @Query(value = """
    	    SELECT distinct `from` FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedFrom(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct subject 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllSubject();

    @Query(value = """
    	    SELECT distinct subject FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedSubject(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct requiredResponse 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllRequiredResponse();

    @Query(value = """
    	    SELECT distinct requiredResponse FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedRequiredResponse(@Param("userId") String userId);

    @Query("select distinct c.dueDate from CorrespondenceLetter c where c.action = 'send'")
    List<String> findAllDueDates();

    @Query("select distinct c.dueDate from CorrespondenceLetter c where c.userId = :userId and c.action = 'send'")
    List<String> findGroupedDueDates(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct projectName 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllProjectNames();

    @Query(value = """
    	    SELECT distinct projectName FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedProjectNames(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct contractName 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllContractNames();

    @Query(value = """
    	    SELECT distinct contractName FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedContractNames(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct currentStatus 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllStatus();

    @Query(value = """
    	    SELECT distinct currentStatus FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedStatus(@Param("userId") String userId);
    @Query(value="""
    		SELECT distinct department 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllDepartment();

    @Query(value = """
    	    SELECT distinct department FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedDepartment(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct attachment 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllAttachment();

    @Query(value = """
    	    SELECT distinct attachment FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedAttachment(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct `type` 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllTypesOfMail();

    @Query(value = """
    	    SELECT distinct `type` FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedTypesOfMail(@Param("userId") String userId);

    @Query(value="""
    		SELECT distinct `to` 
FROM (
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id 
       AND sl.is_cc = 0
    WHERE sl.type = 'Outgoing' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
    
    UNION
    
    SELECT 
        c.category AS category,
        c.letter_number AS letterNumber,
        sl.from_user_name AS `from`,
        sl.to_user_name AS `to`,
        c.subject AS subject,
        c.required_response AS requiredResponse,
        c.due_date AS dueDate,
        c.project_name AS projectName,
        c.contract_name AS contractName,
        c.current_status AS currentStatus,
        c.department AS department,
        c.file_count AS attachment,
        sl.type AS type
    FROM dms.correspondence_letter c
    LEFT JOIN dms.send_correspondence_letter sl 
        ON c.correspondence_id = sl.correspondence_id  
       AND sl.is_cc = 0
    WHERE sl.type = 'Incoming' 
      AND c.action = 'send'
    GROUP BY c.correspondence_id
) x;

    		""", nativeQuery = true)
    List<String> findAllToSend();

    @Query(value = """
    	    SELECT distinct `to` FROM (  
SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`,  
sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName, 
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment, 
 sl.type as type  FROM dms.correspondence_letter c  
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id 
 AND sl.from_user_id = :userId AND sl.is_cc = 0  WHERE sl.type = 'Outgoing' AND c.action = 'send' 
 GROUP BY c.correspondence_id  UNION 
 SELECT c.category as category,  c.letter_number as letterNumber,  sl.from_user_name as `from`, 
 sl.to_user_name as `to`,  c.subject as subject,  c.required_response as requiredResponse, 
 c.due_date as dueDate,  c.project_name as projectName,  c.contract_name as contractName,
 c.current_status as currentStatus,  c.department as department,  c.file_count as attachment,
 sl.type as type  FROM dms.correspondence_letter c 
 LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id  AND sl.to_user_id = :userId  
 WHERE sl.type = 'Incoming' AND c.action = 'send'  GROUP BY c.correspondence_id 
 ) x WHERE 1=1  ORDER BY x.dueDate 
    	    """, nativeQuery = true)
    List<String> findGroupedToSend(@Param("userId") String userId);
    
    @Query("""
    	    SELECT DISTINCT c
    	    FROM CorrespondenceLetter c
    	    LEFT JOIN FETCH c.files f
    	    LEFT JOIN FETCH c.sendCorLetters s
    	    LEFT JOIN FETCH c.correspondenceReferences r
    	    WHERE c.correspondenceId = :id
    	""")
    	Optional<CorrespondenceLetter> findWithAllRelations(@Param("id") Long id);
    
    @Query("""
    	    SELECT DISTINCT c
    	    FROM CorrespondenceLetter c
    	    WHERE c.correspondenceId = :correspondenceId
    	""")
    Optional<CorrespondenceLetter> findByCorrespondenceId(@Param("correspondenceId") Long correspondenceId);
}

