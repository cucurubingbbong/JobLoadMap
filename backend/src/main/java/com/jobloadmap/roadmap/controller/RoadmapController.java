package com.jobloadmap.roadmap.controller;

import com.jobloadmap.roadmap.dto.RoadmapRequest;
import com.jobloadmap.roadmap.dto.RoadmapResponse;
import com.jobloadmap.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping("/from-jd")
    public ResponseEntity<RoadmapResponse> createFromJd(@RequestBody @Valid RoadmapRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        RoadmapResponse response = roadmapService.generateAndSave(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoadmapResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(roadmapService.listForUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(roadmapService.getById(id, authentication.getName()));
    }
}
