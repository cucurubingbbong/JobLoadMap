package com.jrm.app.controller;

import com.jrm.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> payload) {
        try {
            authService.register(
                    payload.getOrDefault("email", ""),
                    payload.getOrDefault("password", ""),
                    payload.getOrDefault("username", "")
            );
            return ResponseEntity.ok(Map.of("message", "가입이 완료되었습니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestBody Map<String, String> payload) {
        try {
            authService.verify(payload.getOrDefault("email", ""), payload.getOrDefault("code", ""));
            return ResponseEntity.ok(Map.of("message", "인증 완료"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/request-reset")
    public ResponseEntity<Map<String, String>> requestReset(@RequestBody Map<String, String> payload) {
        try {
            String code = authService.requestReset(payload.getOrDefault("email", ""));
            return ResponseEntity.ok(Map.of("message", "재설정 코드를 이메일로 발송했습니다. (데모: code 필드 참조)", "code", code));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            authService.resetPassword(
                    payload.getOrDefault("email", ""),
                    payload.getOrDefault("code", ""),
                    payload.getOrDefault("newPassword", "")
            );
            return ResponseEntity.ok(Map.of("message", "비밀번호가 재설정되었습니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {
        try {
            String token = authService.login(payload.getOrDefault("email", ""), payload.getOrDefault("password", ""));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Auth-Token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
