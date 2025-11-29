package com.jrm.app.controller;

import com.jrm.app.model.AttendanceResponse;
import com.jrm.app.service.AttendanceService;
import com.jrm.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AuthService authService;

    public AttendanceController(AttendanceService attendanceService, AuthService authService) {
        this.attendanceService = attendanceService;
        this.authService = authService;
    }

    @PostMapping("/check")
    public ResponseEntity<AttendanceResponse> check(@RequestHeader("X-Auth-Token") String token) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(attendanceService.checkIn(email));
    }

    @GetMapping
    public ResponseEntity<AttendanceResponse> get(@RequestHeader("X-Auth-Token") String token) {
        String email = authService.getEmail(token);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(attendanceService.build(email));
    }
}
