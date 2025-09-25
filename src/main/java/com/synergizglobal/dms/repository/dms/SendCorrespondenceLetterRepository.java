package com.synergizglobal.dms.repository.dms;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.dms.SendCorrespondenceLetter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SendCorrespondenceLetterRepository extends JpaRepository<SendCorrespondenceLetter, Long> {

    @Query("from SendCorrespondenceLetter l where l.correspondenceLetter.correspondenceId=:id")
    List<SendCorrespondenceLetter> findBySendCorrespondenceLetter(@Param("id") Long id);

}
