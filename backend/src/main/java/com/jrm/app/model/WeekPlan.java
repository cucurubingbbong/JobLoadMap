package com.jrm.app.model;

import java.util.List;

public class WeekPlan {
    private int weekNumber;
    private List<String> topics;
    private String mission;
    private String detail;
    private List<String> checklist;

    public WeekPlan() {}

    public WeekPlan(int weekNumber, List<String> topics, String mission) {
        this.weekNumber = weekNumber;
        this.topics = topics;
        this.mission = mission;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) { this.topics = topics; }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) { this.mission = mission; }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<String> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<String> checklist) {
        this.checklist = checklist;
    }
}
