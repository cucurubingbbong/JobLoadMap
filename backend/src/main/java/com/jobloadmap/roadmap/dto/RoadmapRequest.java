package com.jobloadmap.roadmap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RoadmapRequest {

    private String jdUrl;

    private String jdText;

    @Min(1)
    @Max(18)
    private int preparationMonths = 3;

    @Min(1)
    @Max(10)
    private int dailyStudyHours = 3;

    @NotBlank
    private String currentLevel;

    @NotBlank
    private String targetRole;

    private String targetCompany;

    public String getJdUrl() {
        return jdUrl;
    }

    public void setJdUrl(String jdUrl) {
        this.jdUrl = jdUrl;
    }

    public String getJdText() {
        return jdText;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    public int getPreparationMonths() {
        return preparationMonths;
    }

    public void setPreparationMonths(int preparationMonths) {
        this.preparationMonths = preparationMonths;
    }

    public int getDailyStudyHours() {
        return dailyStudyHours;
    }

    public void setDailyStudyHours(int dailyStudyHours) {
        this.dailyStudyHours = dailyStudyHours;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetCompany() {
        return targetCompany;
    }

    public void setTargetCompany(String targetCompany) {
        this.targetCompany = targetCompany;
    }
}
