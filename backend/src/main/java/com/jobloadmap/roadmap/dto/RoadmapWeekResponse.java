package com.jobloadmap.roadmap.dto;

public class RoadmapWeekResponse {
    private int weekNumber;
    private String focusTopics;
    private String practiceMission;
    private boolean completed;

    public RoadmapWeekResponse(int weekNumber, String focusTopics, String practiceMission, boolean completed) {
        this.weekNumber = weekNumber;
        this.focusTopics = focusTopics;
        this.practiceMission = practiceMission;
        this.completed = completed;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public String getFocusTopics() {
        return focusTopics;
    }

    public String getPracticeMission() {
        return practiceMission;
    }

    public boolean isCompleted() {
        return completed;
    }
}
