package com.jobloadmap.roadmap.dto;

import com.jobloadmap.roadmap.model.StepCategory;

import java.util.List;

public class RoadmapStepResponse {
    private Long id;
    private String title;
    private StepCategory category;
    private int orderIndex;
    private int durationWeeks;
    private String summary;
    private List<RoadmapWeekResponse> weeks;

    public RoadmapStepResponse(Long id, String title, StepCategory category, int orderIndex, int durationWeeks, String summary, List<RoadmapWeekResponse> weeks) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.orderIndex = orderIndex;
        this.durationWeeks = durationWeeks;
        this.summary = summary;
        this.weeks = weeks;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public StepCategory getCategory() {
        return category;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public String getSummary() {
        return summary;
    }

    public List<RoadmapWeekResponse> getWeeks() {
        return weeks;
    }
}
