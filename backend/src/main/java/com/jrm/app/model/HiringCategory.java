package com.jrm.app.model;

import java.util.List;

public class HiringCategory {
    private String name;
    private List<JobPost> posts;

    public HiringCategory(String name, List<JobPost> posts) {
        this.name = name;
        this.posts = posts;
    }

    public String getName() {
        return name;
    }

    public List<JobPost> getPosts() {
        return posts;
    }
}
