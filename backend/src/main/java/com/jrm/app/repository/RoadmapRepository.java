package com.jrm.app.repository;

import com.jrm.app.entity.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<RoadmapEntity, String> {
    List<RoadmapEntity> findByEmail(String email);
    List<RoadmapEntity> findByEmailIgnoreCase(String email);
    Optional<RoadmapEntity> findByIdAndEmail(String id, String email);
    Optional<RoadmapEntity> findByIdAndEmailIgnoreCase(String id, String email);
    void deleteByIdAndEmail(String id, String email);
    Optional<RoadmapEntity> findByShareToken(String shareToken);
    boolean existsByIdAndEmail(String id, String email);
}
