package com.jobloadmap.roadmap.model;

import com.jobloadmap.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmap_steps")
public class RoadmapStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id")
    private Roadmap roadmap;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private StepCategory category;

    private int orderIndex;

    private int durationWeeks;

    @Column(length = 1000)
    private String summary;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoadmapWeek> weeks = new ArrayList<>();

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StepCategory getCategory() {
        return category;
    }

    public void setCategory(StepCategory category) {
        this.category = category;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<RoadmapWeek> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<RoadmapWeek> weeks) {
        this.weeks = weeks;
    }

    public void addWeek(RoadmapWeek week) {
        week.setStep(this);
        this.weeks.add(week);
    }
}
