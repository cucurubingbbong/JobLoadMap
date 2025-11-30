package com.jrm.app.controller;

import com.jrm.app.model.CommunityPost;
import com.jrm.app.service.AuthService;
import com.jrm.app.service.CommunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@CrossOrigin
public class CommunityController {
    private final CommunityService communityService;
    private final AuthService authService;

    public CommunityController(CommunityService communityService, AuthService authService) {
        this.communityService = communityService;
        this.authService = authService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> posts() {
        return ResponseEntity.ok(communityService.getPosts());
    }

    @PostMapping("/posts")
    public ResponseEntity<CommunityPost> create(@RequestHeader("X-Auth-Token") String token,
                                                @RequestBody Map<String, String> payload) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            validatePayload(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(413).body(null);
        }
        String displayName = authService.getAccount(token) != null ? authService.getAccount(token).getUsername() : email;
        CommunityPost post = communityService.add(displayName,
                email,
                payload.getOrDefault("title", "공유"),
                payload.getOrDefault("content", ""),
                payload.getOrDefault("category", "전체"),
                payload.get("attachmentName"),
                payload.get("attachmentData"));
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Auth-Token") String token,
                                       @PathVariable("id") String id) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        boolean removed = communityService.delete(id, email);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/posts/{id}")
    public ResponseEntity<CommunityPost> update(@RequestHeader("X-Auth-Token") String token,
                                                @PathVariable("id") String id,
                                                @RequestBody Map<String, String> payload) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            validatePayload(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(413).build();
        }
        return communityService.update(
                        id,
                        email,
                        payload.get("title"),
                        payload.get("content"),
                        payload.get("category"),
                        payload.get("attachmentName"),
                        payload.get("attachmentData"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<CommunityPost> get(@PathVariable("id") String id) {
        return communityService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> comment(@RequestHeader("X-Auth-Token") String token,
                                     @PathVariable("id") String id,
                                     @RequestBody Map<String, String> payload) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        String author = authService.getAccount(token) != null ? authService.getAccount(token).getUsername() : email;
        return communityService.addComment(id, email, author, payload.getOrDefault("content", ""))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void validatePayload(Map<String, String> payload) {
        String title = payload.getOrDefault("title", "");
        String content = payload.getOrDefault("content", "");
        String attachmentData = payload.get("attachmentData");
        if (title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자 이내로 입력해주세요.");
        }
        if (content.length() > 20000) {
            throw new IllegalArgumentException("내용이 너무 깁니다(20,000자 제한).");
        }
        // Base64 문자열 길이 제한 (약 750KB)
        if (attachmentData != null && attachmentData.length() > 1_000_000) {
            throw new IllegalArgumentException("첨부 용량이 초과되었습니다.");
        }
    }
}
