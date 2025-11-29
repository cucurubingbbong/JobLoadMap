package com.jrm.app.controller;

import com.jrm.app.model.JobPost;
import com.jrm.app.service.CrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crawl")
@CrossOrigin
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<JobPost>> getDailyCrawl() {
        // If the scheduler hasn't run yet, force a single load
        if (crawlerService.getCachedPosts().isEmpty()) {
            crawlerService.crawlDaily();
        }
        return ResponseEntity.ok(crawlerService.getCachedPosts());
    }
}
