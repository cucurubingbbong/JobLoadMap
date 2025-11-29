package com.jrm.app.controller;

import com.jrm.app.model.HiringCategory;
import com.jrm.app.service.HiringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import com.jrm.app.service.HiringService;

@RestController
@RequestMapping("/api/hiring")
@CrossOrigin
public class HiringController {

    private final HiringService hiringService;

    public HiringController(HiringService hiringService) {
        this.hiringService = hiringService;
    }

    @GetMapping
    public ResponseEntity<List<HiringCategory>> list() {
        return ResponseEntity.ok(hiringService.list());
    }
}
