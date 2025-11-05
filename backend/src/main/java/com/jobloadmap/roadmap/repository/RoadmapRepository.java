package com.jobloadmap.roadmap.repository;

import com.jobloadmap.auth.model.User;
import com.jobloadmap.roadmap.model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByOwner(User owner);
}
