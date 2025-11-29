package com.jrm.app.model;

import java.time.LocalDate;
import java.util.List;

public class JobPost {
    private String id;
    private String company;
    private String role;
    private String url;
    private String location;
    private LocalDate deadline;
    private List<String> keywords;

    public JobPost(String id, String company, String role, String url, String location, LocalDate deadline, List<String> keywords) {
        this.id = id;
        this.company = company;
        this.role = role;
        this.url = url;
        this.location = location;
        this.deadline = deadline;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getRole() {
        return role;
    }

    public String getUrl() {
        return url;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
