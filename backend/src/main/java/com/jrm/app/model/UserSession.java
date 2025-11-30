package com.jrm.app.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_token", columnList = "token", unique = true)
})
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserAccount user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UserSession() {
    }

    public UserSession(UserAccount user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UserAccount getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
