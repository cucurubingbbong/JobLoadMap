package com.jrm.app.controller;

import com.jrm.app.model.RoadmapRecord;
import com.jrm.app.model.SaveRoadmapRequest;
import com.jrm.app.service.AuthService;
import com.jrm.app.service.RoadmapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE})
public class ProfileController {
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final AuthService authService;
    private final RoadmapStore roadmapStore;

    public ProfileController(AuthService authService, RoadmapStore roadmapStore) {
        this.authService = authService;
        this.roadmapStore = roadmapStore;
    }

    @PostMapping("/roadmaps")
    public ResponseEntity<RoadmapRecord> save(@RequestHeader("X-Auth-Token") String token,
                                               @RequestBody SaveRoadmapRequest payload) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        String title = payload.getTitle() == null ? "나의 로드맵" : payload.getTitle();
        int progress = payload.getProgress() == null ? 0 : payload.getProgress();
        RoadmapRecord record = roadmapStore.save(email, title, progress, payload.getRoadmap());
        return ResponseEntity.ok(record);
    }

    @GetMapping("/roadmaps")
    public ResponseEntity<List<RoadmapRecord>> list(@RequestHeader("X-Auth-Token") String token) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(roadmapStore.list(email));
        } catch (Throwable ex) { // OOM 등도 500으로 보내지 않고 빈 배열로
            log.warn("로드맵 목록 조회 실패, email={}", email, ex);
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @PatchMapping("/roadmaps/{id}/progress")
    public ResponseEntity<RoadmapRecord> updateProgress(@RequestHeader("X-Auth-Token") String token,
                                                        @PathVariable("id") String id,
                                                        @RequestBody Map<String, Integer> payload) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        int progress = payload.getOrDefault("progress", 0);
        RoadmapRecord updated = roadmapStore.updateProgress(email, id, progress);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/roadmaps/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> delete(@RequestHeader("X-Auth-Token") String token,
                                       @PathVariable("id") String id) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        boolean removed = roadmapStore.delete(email, id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(@RequestHeader("X-Auth-Token") String token) {
        var account = authService.getAccount(token);
        if (account == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of(
                "email", account.getEmail(),
                "username", account.getUsername()
        ));
    }

    @PatchMapping("/username")
    public ResponseEntity<Map<String, String>> updateUsername(@RequestHeader("X-Auth-Token") String token,
                                                              @RequestBody Map<String, String> payload) {
        try {
            authService.updateUsername(token, payload.getOrDefault("username", ""));
            return ResponseEntity.ok(Map.of("message", "아이디가 변경되었습니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestHeader("X-Auth-Token") String token,
                                                              @RequestBody Map<String, String> payload) {
        try {
            authService.updatePassword(
                    token,
                    payload.getOrDefault("currentPassword", ""),
                    payload.getOrDefault("newPassword", "")
            );
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/roadmaps/{id}/share")
    public ResponseEntity<Map<String, String>> share(@RequestHeader("X-Auth-Token") String token,
                                                     @PathVariable("id") String id) {
        String email = authService.getEmail(token);
        if (email == null) return ResponseEntity.status(401).build();
        String shareToken = roadmapStore.share(email, id);
        if (shareToken == null) return ResponseEntity.notFound().build();
        String shareUrl = "/api/roadmap/shared/" + shareToken;
        return ResponseEntity.ok(Map.of("token", shareToken, "url", shareUrl));
    }
}
