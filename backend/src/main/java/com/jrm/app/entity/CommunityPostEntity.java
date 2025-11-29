package com.jrm.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "community_posts")
public class CommunityPostEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String attachmentName;

    @Lob
    private String attachmentData;

    private int views;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    private List<CommentEntity> comments = new ArrayList<>();

    public CommunityPostEntity() {
    }

    public CommunityPostEntity(String author, String authorEmail, String title, String content, String category, String attachmentName, String attachmentData) {
        this.id = UUID.randomUUID().toString();
        this.author = author;
        this.authorEmail = authorEmail;
        this.title = title;
        this.content = content;
        this.category = category == null ? "전체" : category;
        this.createdAt = LocalDateTime.now();
        this.attachmentName = attachmentName;
        this.attachmentData = attachmentData;
        this.views = 0;
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
    public List<CommentEntity> getComments() { return comments; }

    public void increaseViews() { this.views++; }
    public void addComment(CommentEntity c) { this.comments.add(c); }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public void setAttachmentData(String attachmentData) { this.attachmentData = attachmentData; }
}
