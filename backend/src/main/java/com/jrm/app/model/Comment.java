package com.jrm.app.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    private final String id;
    private final String authorEmail;
    private final String author;
    private final String content;
    private final LocalDateTime createdAt;

    public Comment(String authorEmail, String author, String content) {
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
