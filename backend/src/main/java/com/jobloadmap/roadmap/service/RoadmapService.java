package com.jobloadmap.roadmap.service;

import com.jobloadmap.auth.model.User;
import com.jobloadmap.auth.repository.UserRepository;
import com.jobloadmap.roadmap.dto.RoadmapRequest;
import com.jobloadmap.roadmap.dto.RoadmapResponse;
import com.jobloadmap.roadmap.dto.RoadmapStepResponse;
import com.jobloadmap.roadmap.dto.RoadmapWeekResponse;
import com.jobloadmap.roadmap.model.Roadmap;
import com.jobloadmap.roadmap.model.RoadmapStep;
import com.jobloadmap.roadmap.model.RoadmapWeek;
import com.jobloadmap.roadmap.repository.RoadmapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapGeneratorService generatorService;
    private final UserRepository userRepository;

    public RoadmapService(RoadmapRepository roadmapRepository,
                          RoadmapGeneratorService generatorService,
                          UserRepository userRepository) {
        this.roadmapRepository = roadmapRepository;
        this.generatorService = generatorService;
        this.userRepository = userRepository;
    }

    @Transactional
    public RoadmapResponse generateAndSave(RoadmapRequest request, String userEmail) {
        User owner = null;
        if (userEmail != null) {
            owner = userRepository.findByEmail(userEmail).orElse(null);
        }
        Roadmap roadmap = generatorService.generateFromRequest(request, owner);
        Roadmap saved = roadmapRepository.save(roadmap);
        return toResponse(saved);
    }

    public List<RoadmapResponse> listForUser(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return roadmapRepository.findByOwner(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RoadmapResponse getById(Long id, String userEmail) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로드맵을 찾을 수 없습니다."));
        if (roadmap.getOwner() != null && !roadmap.getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        return toResponse(roadmap);
    }

    private RoadmapResponse toResponse(Roadmap roadmap) {
        List<RoadmapStepResponse> steps = roadmap.getSteps().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(step -> new RoadmapStepResponse(
                        step.getId(),
                        step.getTitle(),
                        step.getCategory(),
                        step.getOrderIndex(),
                        step.getDurationWeeks(),
                        step.getSummary(),
                        toWeekResponses(step.getWeeks())
                ))
                .collect(Collectors.toList());
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getTargetCompany(),
                roadmap.getTargetRole(),
                roadmap.getTotalWeeks(),
                roadmap.getDailyStudyHours(),
                roadmap.getLevel(),
                roadmap.getSourceUrl(),
                steps
        );
    }

    private List<RoadmapWeekResponse> toWeekResponses(List<RoadmapWeek> weeks) {
        return weeks.stream()
                .sorted((a, b) -> Integer.compare(a.getWeekNumber(), b.getWeekNumber()))
                .map(week -> new RoadmapWeekResponse(
                        week.getWeekNumber(),
                        week.getFocusTopics(),
                        week.getPracticeMission(),
                        week.isCompleted()
                ))
                .collect(Collectors.toList());
    }
}
