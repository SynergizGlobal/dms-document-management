package com.synergizglobal.dms.repository.dms;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.dms.Folder;
import com.synergizglobal.dms.entity.dms.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    boolean existsByName(String name);

	Optional<Status> findByName(String currentStatus);
}
