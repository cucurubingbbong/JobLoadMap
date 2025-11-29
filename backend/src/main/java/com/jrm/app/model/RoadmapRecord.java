package com.jrm.app.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RoadmapRecord {
    private String id;
    private final String title;
    private LocalDate createdAt;
    private int progress;
    private final RoadmapResponse roadmap;

    public RoadmapRecord(String title, int progress, RoadmapResponse roadmap) {
        this(UUID.randomUUID().toString(), title, LocalDate.now(), progress, roadmap);
    }

    public RoadmapRecord(String id, String title, LocalDate createdAt, int progress, RoadmapResponse roadmap) {
        this.id = id;
        this.title = title;
        this.progress = progress;
        this.roadmap = roadmap;
        this.createdAt = createdAt == null ? LocalDate.now() : createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public RoadmapResponse getRoadmap() {
        return roadmap;
    }

    // setters for mapper use
    public void setId(String id) { this.id = id; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
