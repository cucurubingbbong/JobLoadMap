package com.jrm.app.controller;

import com.jrm.app.model.RoadmapRequest;
import com.jrm.app.model.RoadmapResponse;
import com.jrm.app.model.ChatRequest;
import com.jrm.app.service.RoadmapEngine;
import com.jrm.app.service.AiAdapter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/roadmap")
@CrossOrigin
public class RoadmapController {

    private final RoadmapEngine roadmapEngine;
    private final AiAdapter aiAdapter;
    private final com.jrm.app.service.RoadmapStore roadmapStore;

    public RoadmapController(RoadmapEngine roadmapEngine, AiAdapter aiAdapter, com.jrm.app.service.RoadmapStore roadmapStore) {
        this.roadmapEngine = roadmapEngine;
        this.aiAdapter = aiAdapter;
        this.roadmapStore = roadmapStore;
    }

    @PostMapping("/from-jd")
    public ResponseEntity<RoadmapResponse> fromJd(@Valid @RequestBody RoadmapRequest request) {
        try {
            return ResponseEntity.ok(roadmapEngine.generate(request));
        } catch (Exception ex) {
            return ResponseEntity.status(502).build();
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest payload) {
        try {
            if (payload.getRoadmap() == null || payload.getMessage() == null) {
                return ResponseEntity.badRequest().body(Map.of("answer", "message와 roadmap이 필요합니다."));
            }
            String answer = aiAdapter.chatRoadmap(payload.getMessage(), payload.getRoadmap());
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception ex) {
            return ResponseEntity.ok(Map.of("answer", "AI 응답에 실패했습니다. 설정과 키를 확인해주세요."));
        }
    }

    @GetMapping("/shared/{token}")
    public ResponseEntity<?> shared(@PathVariable("token") String token) {
        var record = roadmapStore.findByShareToken(token);
        if (record == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(record);
    }
}
