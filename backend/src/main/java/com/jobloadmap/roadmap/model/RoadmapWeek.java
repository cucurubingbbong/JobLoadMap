package com.jobloadmap.roadmap.model;

import com.jobloadmap.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "roadmap_weeks")
public class RoadmapWeek extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private RoadmapStep step;

    private int weekNumber;

    @Column(length = 1000)
    private String focusTopics;

    @Column(length = 1000)
    private String practiceMission;

    private boolean completed;

    public RoadmapStep getStep() {
        return step;
    }

    public void setStep(RoadmapStep step) {
        this.step = step;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getFocusTopics() {
        return focusTopics;
    }

    public void setFocusTopics(String focusTopics) {
        this.focusTopics = focusTopics;
    }

    public String getPracticeMission() {
        return practiceMission;
    }

    public void setPracticeMission(String practiceMission) {
        this.practiceMission = practiceMission;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
