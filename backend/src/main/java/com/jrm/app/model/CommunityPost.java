package com.jrm.app.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommunityPost {
    private final String id;
    private final String author;
    private final String authorEmail;
    private final String title;
    private final String content;
    private final String category;
    private final LocalDateTime createdAt;
    private final String attachmentName;
    private final String attachmentData;
    private int views = 0;
    private final java.util.List<Comment> comments = new java.util.ArrayList<>();

    public CommunityPost(String author, String authorEmail, String title, String content, String category) {
        this(author, authorEmail, title, content, category, null, null);
    }

    public CommunityPost(String author, String authorEmail, String title, String content, String category, String attachmentName, String attachmentData) {
        this.id = UUID.randomUUID().toString();
        this.author = author;
        this.authorEmail = authorEmail;
        this.title = title;
        this.content = content;
        this.category = category == null ? "전체" : category;
        this.createdAt = LocalDateTime.now();
        this.attachmentName = attachmentName;
        this.attachmentData = attachmentData;
    }

    public String getId() { return id; }
    public String getAuthor() { return author; }
    public String getAuthorEmail() { return authorEmail; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAttachmentName() { return attachmentName; }
    public String getAttachmentData() { return attachmentData; }
    public int getViews() { return views; }
    public void increaseViews() { this.views++; }
    public java.util.List<Comment> getComments() { return comments; }
    public void addComment(Comment c) { this.comments.add(c); }
}
