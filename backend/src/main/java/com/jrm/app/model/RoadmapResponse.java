package com.jrm.app.model;

import java.util.List;

public class RoadmapResponse {
    private String targetRole;
    private int totalWeeks;
    private int dailyHours;
    private List<String> keywords;
    private List<RoadmapStep> steps;
    private boolean aiGenerated;

    public RoadmapResponse() {}

    public RoadmapResponse(String targetRole, int totalWeeks, int dailyHours, List<String> keywords, List<RoadmapStep> steps) {
        this.targetRole = targetRole;
        this.totalWeeks = totalWeeks;
        this.dailyHours = dailyHours;
        this.keywords = keywords;
        this.steps = steps;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public int getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(int totalWeeks) { this.totalWeeks = totalWeeks; }

    public int getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(int dailyHours) { this.dailyHours = dailyHours; }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<RoadmapStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RoadmapStep> steps) { this.steps = steps; }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
