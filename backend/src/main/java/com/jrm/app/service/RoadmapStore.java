package com.jrm.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrm.app.entity.RoadmapEntity;
import com.jrm.app.model.RoadmapRecord;
import com.jrm.app.model.RoadmapResponse;
import com.jrm.app.repository.RoadmapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoadmapStore {
    private final RoadmapRepository roadmapRepository;
    private final ObjectMapper objectMapper;

    public RoadmapStore(RoadmapRepository roadmapRepository, ObjectMapper objectMapper) {
        this.roadmapRepository = roadmapRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RoadmapRecord save(String email, String title, int progress, RoadmapResponse roadmap) {
        String json = toJson(roadmap);
        RoadmapEntity entity = new RoadmapEntity(email, title, progress, json);
        roadmapRepository.save(entity);
        return toRecord(entity);
    }

    public List<RoadmapRecord> list(String email) {
        return roadmapRepository.findByEmail(email).stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoadmapRecord updateProgress(String email, String recordId, int progress) {
        Optional<RoadmapEntity> entityOpt = roadmapRepository.findByIdAndEmail(recordId, email);
        if (entityOpt.isEmpty()) return null;
        RoadmapEntity entity = entityOpt.get();
        entity.setProgress(progress);
        roadmapRepository.save(entity);
        return toRecord(entity);
    }

    @Transactional
    public boolean delete(String email, String recordId) {
        Optional<RoadmapEntity> entityOpt = roadmapRepository.findByIdAndEmail(recordId, email);
        if (entityOpt.isPresent()) {
            roadmapRepository.delete(entityOpt.get());
            return true;
        }
        // fallback in case the entity was not loaded but exists
        boolean exists = roadmapRepository.existsByIdAndEmail(recordId, email);
        if (exists) {
            roadmapRepository.deleteById(recordId);
            return true;
        }
        return false;
    }

    @Transactional
    public String share(String email, String recordId) {
        Optional<RoadmapEntity> entityOpt = roadmapRepository.findByIdAndEmail(recordId, email);
        if (entityOpt.isEmpty()) return null;
        RoadmapEntity entity = entityOpt.get();
        if (entity.getShareToken() == null || entity.getShareToken().isBlank()) {
            entity.setShareToken(java.util.UUID.randomUUID().toString());
            roadmapRepository.save(entity);
        }
        return entity.getShareToken();
    }

    public RoadmapRecord findByShareToken(String token) {
        return roadmapRepository.findByShareToken(token)
                .map(this::toRecord)
                .orElse(null);
    }

    private RoadmapRecord toRecord(RoadmapEntity entity) {
        RoadmapResponse roadmap = fromJson(entity.getRoadmapJson());
        return new RoadmapRecord(entity.getId(), entity.getTitle(), entity.getCreatedAt(), entity.getProgress(), roadmap);
    }

    private String toJson(RoadmapResponse r) {
        try {
            return objectMapper.writeValueAsString(r);
        } catch (Exception e) {
            throw new RuntimeException("로드맵 직렬화 실패", e);
        }
    }

    private RoadmapResponse fromJson(String json) {
        try {
            return objectMapper.readValue(json, RoadmapResponse.class);
        } catch (IOException e) {
            return new RoadmapResponse();
        }
    }
}
