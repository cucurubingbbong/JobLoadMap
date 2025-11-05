package com.jobloadmap.roadmap.model;

import com.jobloadmap.auth.model.User;
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
@Table(name = "roadmaps")
public class Roadmap extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String targetCompany;

    private String targetRole;

    @Column(nullable = false)
    private int totalWeeks;

    @Column(nullable = false)
    private double dailyStudyHours;

    @Column(nullable = false)
    private String level;

    private String sourceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoadmapStep> steps = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetCompany() {
        return targetCompany;
    }

    public void setTargetCompany(String targetCompany) {
        this.targetCompany = targetCompany;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(int totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public void setDailyStudyHours(double dailyStudyHours) {
        this.dailyStudyHours = dailyStudyHours;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<RoadmapStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RoadmapStep> steps) {
        this.steps = steps;
    }

    public void addStep(RoadmapStep step) {
        step.setRoadmap(this);
        this.steps.add(step);
    }
}
