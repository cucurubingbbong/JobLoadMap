package com.jrm.app.service;

import com.jrm.app.model.RoadmapRequest;
import com.jrm.app.model.RoadmapResponse;
import com.jrm.app.model.RoadmapStep;
import com.jrm.app.model.WeekPlan;
import com.jrm.app.service.AiAdapter;
import com.jrm.app.service.JdFetcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoadmapEngine {

    private final JdFetcher jdFetcher;
    private final AiAdapter aiAdapter;

    public RoadmapEngine(JdFetcher jdFetcher, AiAdapter aiAdapter) {
        this.jdFetcher = jdFetcher;
        this.aiAdapter = aiAdapter;
    }

    private static final Map<String, List<String>> PREREQUISITES = Map.ofEntries(
            Map.entry("spring", List.of("Java OOP", "HTTP", "MVC", "Database basics")),
            Map.entry("react", List.of("JavaScript ES6", "DOM", "HTML/CSS", "State management")),
            Map.entry("aws", List.of("Linux CLI", "Networking", "Deployment pipeline")),
            Map.entry("mysql", List.of("SQL", "Indexes", "Transactions")),
            Map.entry("docker", List.of("Containers", "Images", "Compose")),
            Map.entry("kotlin", List.of("JVM", "Null safety", "Coroutine basics")),
            Map.entry("typescript", List.of("Type system", "TS + React", "Tooling")),
            Map.entry("vue", List.of("Composition API", "Routing", "State")),
            Map.entry("node", List.of("npm", "Express", "API security")),
            Map.entry("next", List.of("SSR", "Routing", "Data fetching")),
            Map.entry("nestjs", List.of("Controllers", "Providers", "Testing")),
            Map.entry("redis", List.of("Caching", "Pub/Sub", "TTL 전략")),
            Map.entry("mongodb", List.of("Document schema", "Index 설계", "Aggregation")),
            Map.entry("kafka", List.of("프로듀서/컨슈머", "토픽 설계", "Exactly-once")),
            Map.entry("jenkins", List.of("파이프라인", "빌드", "배포 자동화"))
    );

    public RoadmapResponse generate(RoadmapRequest request) {
        String jdBody = jdFetcher.resolveJdText(request.getJdText(), request.getJdUrl());
        RoadmapResponse r = aiAdapter.generateRoadmapStrict(jdBody, request);
        if (r == null || r.getSteps() == null || r.getSteps().isEmpty()) {
            throw new IllegalStateException("Gemini 로드맵 생성 실패: steps 없음");
        }
        if (r.getTotalWeeks() == 0) {
            r.setTotalWeeks(Math.max(4, request.getDurationMonths() * 4));
        }
        if (r.getDailyHours() == 0) {
            r.setDailyHours(request.getDailyHours());
        }
        r.setAiGenerated(true);
        return r;
    }

    private RoadmapResponse buildFallbackRoadmap(RoadmapRequest request, String jdBody) {
        List<String> keywords = extractKeywords(jdBody);
        int durationWeeks = Math.max(4, request.getDurationMonths() * 4);

        List<Integer> stepWeekAllocation = distributeWeeks(durationWeeks);
        List<RoadmapStep> steps = new ArrayList<>();

        int cursorWeek = 1;
        int fundamentalsWeeks = stepWeekAllocation.get(0);
        steps.add(new RoadmapStep(
                "Step 1. 선수지식",
                fundamentalsWeeks,
                buildFundamentals(cursorWeek, fundamentalsWeeks)
        ));
        cursorWeek += fundamentalsWeeks;

        int coreWeeks = stepWeekAllocation.get(1);
        steps.add(new RoadmapStep(
                "Step 2. 핵심기술",
                coreWeeks,
                buildCoreWeeks(cursorWeek, coreWeeks, keywords)
        ));
        cursorWeek += coreWeeks;

        int practicalWeeks = stepWeekAllocation.get(2);
        steps.add(new RoadmapStep(
                "Step 3. 실무스택",
                practicalWeeks,
                buildPractical(cursorWeek, practicalWeeks)
        ));
        cursorWeek += practicalWeeks;

        int bonusWeeks = stepWeekAllocation.get(3);
        steps.add(new RoadmapStep(
                "Step 4. 보너스스킬",
                bonusWeeks,
                buildBonus(cursorWeek, bonusWeeks, request.getLevel())
        ));

        return new RoadmapResponse("JD 기반 맞춤 로드맵", durationWeeks, request.getDailyHours(), keywords, steps);
    }
    private List<String> extractKeywords(String jdText) {
        String lowered = jdText == null ? "" : jdText.toLowerCase(Locale.ROOT);
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        PREREQUISITES.keySet().forEach(key -> {
            if (lowered.contains(key)) {
                unique.add(key);
            }
        });
        if (unique.isEmpty()) {
            unique.addAll(Arrays.asList("java", "spring", "aws"));
        }
        return new ArrayList<>(unique);
    }

    private List<WeekPlan> buildFundamentals(int startWeek, int totalWeeks) {
        List<WeekPlan> plans = new ArrayList<>();
        List<List<String>> fundamentals = List.of(
                List.of("CS 기초", "자료구조/알고리즘", "HTTP/네트워크"),
                List.of("Git/GitHub", "리눅스 CLI", "REST API"),
                List.of("SQL", "RDB 설계", "트랜잭션"),
                List.of("테스트 주도 개발", "리팩토링"),
                List.of("UI/UX 기초", "접근성"),
                List.of("클라우드 이해", "배포 파이프라인")
        );

        for (int i = 0; i < totalWeeks; i++) {
            List<String> topics = fundamentals.get(Math.min(i, fundamentals.size() - 1));
            plans.add(new WeekPlan(startWeek + i, topics, "핵심 요약 정리 + 미니 실습"));
        }
        return plans;
    }

    private List<WeekPlan> buildCoreWeeks(int startWeek, int totalWeeks, List<String> keywords) {
        List<WeekPlan> plans = new ArrayList<>();
        List<String> limitedKeywords = keywords.stream()
                .limit(Math.max(2, totalWeeks))
                .collect(Collectors.toList());

        Map<String, String> missionCache = new LinkedHashMap<>();

        for (int i = 0; i < totalWeeks; i++) {
            String keyword = limitedKeywords.get(i % limitedKeywords.size());
            List<String> topics = PREREQUISITES.getOrDefault(keyword, List.of(keyword + " 기본기"));
            String mission = missionCache.computeIfAbsent(keyword, k ->
                    aiAdapter.summarizeTopics(topics).stream().findFirst().orElse("튜토리얼 완주")
            );
            plans.add(new WeekPlan(startWeek + i, topics, mission));
        }
        return plans;
    }

    private List<WeekPlan> buildPractical(int startWeek, int totalWeeks) {
        List<WeekPlan> plans = new ArrayList<>();
        List<List<String>> stacks = List.of(
                List.of("서비스 설계", "API 명세", "도메인 모델링"),
                List.of("테스트 자동화", "품질 측정", "코드 리뷰"),
                List.of("보안/인증", "로그/모니터링"),
                List.of("도커라이징", "CI/CD"),
                List.of("클라우드 배포", "비용 최적화"),
                List.of("팀 협업 실습", "애자일 플로우")
        );

        for (int i = 0; i < totalWeeks; i++) {
            List<String> topics = stacks.get(Math.min(i, stacks.size() - 1));
            plans.add(new WeekPlan(startWeek + i, topics, "서비스 기능 고도화/운영 실습"));
        }
        return plans;
    }

    private List<WeekPlan> buildBonus(int startWeek, int totalWeeks, String level) {
        List<WeekPlan> plans = new ArrayList<>();
        List<List<String>> bonus = List.of(
                List.of("기술 블로그", "면접 기록"),
                List.of("오픈소스 기여", "테크 토크"),
                List.of("알고리즘 실전", "CS 심화"),
                List.of("포트폴리오 리팩토링", "프로덕트 스토리텔링")
        );
        String safeLevel = level == null ? "" : level;

        for (int i = 0; i < totalWeeks; i++) {
            List<String> topics = bonus.get(Math.min(i, bonus.size() - 1));
            String mission = safeLevel.contains("입문") || safeLevel.toLowerCase(Locale.ROOT).contains("beginner")
                    ? "기초 개념을 글로 정리"
                    : "깃허브에 실습 결과물 업로드";
            plans.add(new WeekPlan(startWeek + i, topics, mission));
        }
        return plans;
    }

    private List<Integer> distributeWeeks(int durationWeeks) {
        List<Integer> weights = List.of(3, 4, 4, 2);
        int weightSum = weights.stream().mapToInt(Integer::intValue).sum();
        List<Integer> allocation = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < weights.size(); i++) {
            int weeks = (int) Math.round((weights.get(i) / (double) weightSum) * durationWeeks);
            weeks = Math.max(1, weeks);
            allocation.add(weeks);
            assigned += weeks;
        }
        // Adjust to exact duration
        while (assigned > durationWeeks) {
            for (int i = allocation.size() - 1; i >= 0 && assigned > durationWeeks; i--) {
                if (allocation.get(i) > 1) {
                    allocation.set(i, allocation.get(i) - 1);
                    assigned--;
                }
            }
        }
        while (assigned < durationWeeks) {
            for (int i = 0; i < allocation.size() && assigned < durationWeeks; i++) {
                allocation.set(i, allocation.get(i) + 1);
                assigned++;
            }
        }
        return allocation;
    }
}
