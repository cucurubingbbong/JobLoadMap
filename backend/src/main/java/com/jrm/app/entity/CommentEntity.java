package com.jrm.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_comments")
public class CommentEntity {
    @Id
    private String id;

    private String authorEmail;
    private String author;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public CommentEntity() {
    }

    public CommentEntity(String authorEmail, String author, String content) {
        this.id = UUID.randomUUID().toString();
        this.authorEmail = authorEmail;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
