package com.jrm.app.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
public class RoadmapEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int progress;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String roadmapJson;

    @Column(unique = true)
    private String shareToken;

    public RoadmapEntity() {
    }

    public RoadmapEntity(String email, String title, int progress, String roadmapJson) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.title = title;
        this.progress = progress;
        this.createdAt = LocalDate.now();
        this.roadmapJson = roadmapJson;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getTitle() { return title; }
    public int getProgress() { return progress; }
    public LocalDate getCreatedAt() { return createdAt; }
    public String getRoadmapJson() { return roadmapJson; }
    public String getShareToken() { return shareToken; }

    public void setProgress(int progress) { this.progress = progress; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }
}
