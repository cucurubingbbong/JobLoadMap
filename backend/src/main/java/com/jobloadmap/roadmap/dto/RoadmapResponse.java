package com.jobloadmap.roadmap.dto;

import java.util.List;

public class RoadmapResponse {
    private Long id;
    private String title;
    private String targetCompany;
    private String targetRole;
    private int totalWeeks;
    private double dailyStudyHours;
    private String level;
    private String sourceUrl;
    private List<RoadmapStepResponse> steps;

    public RoadmapResponse(Long id, String title, String targetCompany, String targetRole, int totalWeeks, double dailyStudyHours, String level, String sourceUrl, List<RoadmapStepResponse> steps) {
        this.id = id;
        this.title = title;
        this.targetCompany = targetCompany;
        this.targetRole = targetRole;
        this.totalWeeks = totalWeeks;
        this.dailyStudyHours = dailyStudyHours;
        this.level = level;
        this.sourceUrl = sourceUrl;
        this.steps = steps;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTargetCompany() {
        return targetCompany;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public int getTotalWeeks() {
        return totalWeeks;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public String getLevel() {
        return level;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public List<RoadmapStepResponse> getSteps() {
        return steps;
    }
}
