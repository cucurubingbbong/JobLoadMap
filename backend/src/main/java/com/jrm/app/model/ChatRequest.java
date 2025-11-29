package com.jrm.app.model;

public class ChatRequest {
    private String message;
    private RoadmapResponse roadmap;
    private Integer progress;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RoadmapResponse getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(RoadmapResponse roadmap) {
        this.roadmap = roadmap;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}
