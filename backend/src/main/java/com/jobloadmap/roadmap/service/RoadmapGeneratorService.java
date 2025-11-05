package com.jobloadmap.roadmap.service;

import com.jobloadmap.auth.model.User;
import com.jobloadmap.roadmap.dto.RoadmapRequest;
import com.jobloadmap.roadmap.model.Roadmap;
import com.jobloadmap.roadmap.model.RoadmapStep;
import com.jobloadmap.roadmap.model.RoadmapWeek;
import com.jobloadmap.roadmap.model.StepCategory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoadmapGeneratorService {

    private final SkillGraphLoader skillGraphLoader;
    private final KeywordExtractorService keywordExtractorService;

    public RoadmapGeneratorService(SkillGraphLoader skillGraphLoader, KeywordExtractorService keywordExtractorService) {
        this.skillGraphLoader = skillGraphLoader;
        this.keywordExtractorService = keywordExtractorService;
    }

    public Roadmap generateFromRequest(RoadmapRequest request, User owner) {
        String jdText = resolveJdText(request);
        if (!StringUtils.hasText(jdText)) {
            throw new IllegalArgumentException("채용 공고 내용을 불러오지 못했습니다.");
        }

        Set<String> matchedSkills = keywordExtractorService.extractSkillIds(jdText);
        if (matchedSkills.isEmpty()) {
            matchedSkills.addAll(defaultSkillsForTarget(request.getTargetRole()));
        }

        Set<String> expandedSkills = expandWithPrerequisites(matchedSkills);
        List<SkillDefinition> orderedSkills = expandedSkills.stream()
                .map(skillGraphLoader::getSkill)
                .filter(def -> def != null)
                .sorted(Comparator
                        .comparingInt((SkillDefinition def) -> categoryOrder(def.getCategory()))
                        .thenComparing(SkillDefinition::getDisplayName))
                .collect(Collectors.toList());

        int totalWeeks = Math.max(request.getPreparationMonths() * 4, 4);
        double weeklyCapacity = request.getDailyStudyHours() * 7d;

        Roadmap roadmap = new Roadmap();
        roadmap.setOwner(owner);
        roadmap.setTitle(request.getTargetRole() + " 로드맵");
        roadmap.setTargetCompany(request.getTargetCompany());
        roadmap.setTargetRole(request.getTargetRole());
        roadmap.setTotalWeeks(totalWeeks);
        roadmap.setDailyStudyHours(request.getDailyStudyHours());
        roadmap.setLevel(request.getCurrentLevel());
        roadmap.setSourceUrl(request.getJdUrl());

        int globalWeekCounter = 1;
        int stepIndex = 0;

        for (SkillDefinition skill : orderedSkills) {
            RoadmapStep step = new RoadmapStep();
            step.setCategory(skill.getCategory());
            step.setTitle(buildStepTitle(skill));
            step.setOrderIndex(stepIndex++);
            step.setSummary(skill.getDescription());

            int weeksForSkill = Math.max(1, (int) Math.ceil(skill.getEstimatedHours() / weeklyCapacity));
            step.setDurationWeeks(weeksForSkill);

            Queue<String> topicQueue = new ArrayDeque<>(skill.getTopics());
            for (int i = 0; i < weeksForSkill; i++) {
                RoadmapWeek week = new RoadmapWeek();
                week.setWeekNumber(globalWeekCounter++);
                List<String> focus = new ArrayList<>();
                for (int j = 0; j < 3 && !topicQueue.isEmpty(); j++) {
                    focus.add(topicQueue.poll());
                }
                if (focus.isEmpty()) {
                    focus.add(skill.getDisplayName() + " 실습");
                }
                week.setFocusTopics(String.join(", ", focus));
                week.setPracticeMission("\u2615 " + skill.getDisplayName() + " 관련 미니 프로젝트 또는 실습을 진행하세요.");
                week.setCompleted(false);
                step.addWeek(week);
            }
            roadmap.addStep(step);
        }

        roadmap.setTotalWeeks(Math.max(roadmap.getSteps().stream().mapToInt(RoadmapStep::getDurationWeeks).sum(), totalWeeks));
        return roadmap;
    }

    private String resolveJdText(RoadmapRequest request) {
        try {
            if (StringUtils.hasText(request.getJdText())) {
                return request.getJdText();
            }
            if (StringUtils.hasText(request.getJdUrl())) {
                return keywordExtractorService.fetchTextFromUrl(request.getJdUrl());
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("JD를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        return null;
    }

    private Set<String> defaultSkillsForTarget(String targetRole) {
        String lower = targetRole.toLowerCase();
        if (lower.contains("back")) {
            return Set.of("java", "spring", "spring-boot", "sql", "mysql", "rest-api", "git-basics");
        }
        if (lower.contains("front")) {
            return Set.of("frontend-basics", "javascript", "react", "git-basics");
        }
        if (lower.contains("data")) {
            return Set.of("programming-basics", "python", "sql", "datastructures", "algorithms");
        }
        return Set.of("programming-basics", "git-basics", "datastructures");
    }

    private Set<String> expandWithPrerequisites(Set<String> skills) {
        Set<String> expanded = new HashSet<>(skills);
        Queue<String> queue = new ArrayDeque<>(skills);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            SkillDefinition definition = skillGraphLoader.getSkill(current);
            if (definition == null) {
                continue;
            }
            for (String prereq : definition.getPrerequisites()) {
                if (expanded.add(prereq)) {
                    queue.add(prereq);
                }
            }
        }
        return expanded;
    }

    private int categoryOrder(StepCategory category) {
        return switch (category) {
            case FOUNDATIONAL -> 0;
            case CORE -> 1;
            case PRODUCTION -> 2;
            case BONUS -> 3;
        };
    }

    private String buildStepTitle(SkillDefinition skill) {
        return switch (skill.getCategory()) {
            case FOUNDATIONAL -> "Step " + skill.getDisplayName() + " - 선수지식";
            case CORE -> "Step " + skill.getDisplayName() + " - 핵심기술";
            case PRODUCTION -> "Step " + skill.getDisplayName() + " - 실무스택";
            case BONUS -> "Step " + skill.getDisplayName() + " - 보너스";
        };
    }
}
