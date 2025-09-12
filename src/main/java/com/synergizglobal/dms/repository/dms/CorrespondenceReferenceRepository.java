package com.synergizglobal.dms.repository.dms;

import com.synergizglobal.dms.entity.dms.CorrespondenceReference;
import com.synergizglobal.dms.entity.dms.CorrespondenceReferenceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrespondenceReferenceRepository extends JpaRepository<CorrespondenceReference, CorrespondenceReferenceId> {
}
