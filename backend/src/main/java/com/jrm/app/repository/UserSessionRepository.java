package com.jrm.app.repository;

import com.jrm.app.model.UserAccount;
import com.jrm.app.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(UserAccount user);
}
