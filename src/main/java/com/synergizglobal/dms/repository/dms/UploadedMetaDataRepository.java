package com.synergizglobal.dms.repository.dms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.UploadedMetaData;

@Repository
public interface UploadedMetaDataRepository extends JpaRepository<UploadedMetaData, Long>{
	@Query("SELECT u FROM UploadedMetaData u WHERE u.uploadedBy = :userId AND (u.processed IS NULL OR u.processed <> true)")
	List<UploadedMetaData> findByUserIdNotProcessed(@Param("userId") String userId);

}
