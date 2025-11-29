package com.jrm.app.model;

public class SaveRoadmapRequest {
    private String title;
    private Integer progress;
    private RoadmapResponse roadmap;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public RoadmapResponse getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(RoadmapResponse roadmap) {
        this.roadmap = roadmap;
    }
}
