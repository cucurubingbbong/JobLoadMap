package com.jrm.app.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoadmapRequest {
    private String jdText;

    private String jdUrl;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer durationMonths;

    @NotNull
    @Min(1)
    @Max(8)
    private Integer dailyHours;

    @NotBlank
    private String level;

    public String getJdText() {
        return jdText;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    public String getJdUrl() {
        return jdUrl;
    }

    public void setJdUrl(String jdUrl) {
        this.jdUrl = jdUrl;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public Integer getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(Integer dailyHours) {
        this.dailyHours = dailyHours;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
