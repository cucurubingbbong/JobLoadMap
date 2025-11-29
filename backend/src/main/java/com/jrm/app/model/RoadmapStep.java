package com.jrm.app.model;

import java.util.List;

public class RoadmapStep {
    private String title;
    private int estimatedWeeks;
    private List<WeekPlan> weeks;

    public RoadmapStep() {}

    public RoadmapStep(String title, int estimatedWeeks, List<WeekPlan> weeks) {
        this.title = title;
        this.estimatedWeeks = estimatedWeeks;
        this.weeks = weeks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public int getEstimatedWeeks() {
        return estimatedWeeks;
    }

    public void setEstimatedWeeks(int estimatedWeeks) { this.estimatedWeeks = estimatedWeeks; }

    public List<WeekPlan> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<WeekPlan> weeks) { this.weeks = weeks; }
}
