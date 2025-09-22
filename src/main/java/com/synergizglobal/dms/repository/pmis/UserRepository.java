package com.synergizglobal.dms.repository.pmis;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synergizglobal.dms.entity.pmis.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByUserNameContainingIgnoreCase(String userName);

    Optional<User>findByEmailId(String sendTo);

   // @Query("select u from User u where u.emailId = :email")
    ///Optional<User> findByEmailId(String emailId);
    Optional<User> findByUserName(String userName);
}