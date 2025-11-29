package com.jrm.app.controller;

import com.jrm.app.service.AiAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin
public class HealthController {

    private final AiAdapter aiAdapter;

    public HealthController(AiAdapter aiAdapter) {
        this.aiAdapter = aiAdapter;
    }

    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> ai() {
        boolean ok = aiAdapter.ping();
        return ResponseEntity.ok(Map.of("aiConnected", ok));
    }
}
