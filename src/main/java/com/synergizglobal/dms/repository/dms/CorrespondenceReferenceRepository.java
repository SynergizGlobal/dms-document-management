package com.synergizglobal.dms.repository.dms;

import com.synergizglobal.dms.entity.dms.CorrespondenceReference;
import com.synergizglobal.dms.entity.dms.CorrespondenceReferenceId;
import com.synergizglobal.dms.entity.dms.ReferenceLetter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface CorrespondenceReferenceRepository extends JpaRepository<CorrespondenceReference, CorrespondenceReferenceId> {

	@Query("SELECT cr.referenceLetter FROM CorrespondenceReference cr " +
            "WHERE cr.correspondenceLetter.correspondenceId = :correspondenceId")
    List<ReferenceLetter> findReferenceLettersByCorrespondenceId(@Param("correspondenceId") Long correspondenceId);

}
