package com.jrm.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrm.app.model.RoadmapRequest;
import com.jrm.app.model.RoadmapResponse;
import com.jrm.app.model.RoadmapStep;
import com.jrm.app.model.WeekPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AiAdapter {
    private static final Logger log = LoggerFactory.getLogger(AiAdapter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ROADMAP_INSTRUCTION =
            "You are an expert career coach. Read the job description (JD) text and produce a structured JSON roadmap.\n"
                    + "Return ONLY JSON, no prose. JSON schema:\n"
                    + "{\n"
                    + "  \"targetRole\": \"string\",\n"
                    + "  \"totalWeeks\": number,\n"
                    + "  \"dailyHours\": number,\n"
                    + "  \"keywords\": [\"string\"],\n"
                    + "  \"steps\": [\n"
                    + "     { \"title\": \"string\", \"estimatedWeeks\": number,\n"
                    + "       \"weeks\": [\n"
                    + "          { \"weekNumber\": number, \"topics\": [\"string\"], \"mission\": \"string\" }\n"
                    + "       ]\n"
                    + "     }\n"
                    + "  ]\n"
                    + "}\n"
                    + "Ensure weeks sum roughly to totalWeeks. Keep topics concise. If data is missing, infer sensible defaults.";

    private static final String MISSION_INSTRUCTION =
            "Summarize each topic into a short actionable mission.\n"
                    + "Return ONLY JSON: { \"missions\": [\"...\",\"...\"] }";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;
    private final String apiKey;

    public AiAdapter(
            @Value("${ai.api.url:}") String apiUrl,
            @Value("${ai.api.key:}") String apiKey
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    /**
     * 토픽 목록을 미션 문구로 요약 (외부 설정 없거나 실패 시 기본 미션 반환)
     */
    public List<String> summarizeTopics(List<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return Collections.emptyList();
        }
        if (hasExternalConfig()) {
            try {
                return callSummaries(topics);
            } catch (Exception ex) {
                logHttpError("AI 미션 요약 실패, 기본 미션 사용", ex);
            }
        }
        return fallbackMissions(topics);
    }

    private boolean hasExternalConfig() {
        return apiUrl != null && !apiUrl.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    /**
     * 단순 Ping으로 AI 연결 여부 확인 (최소 generateContent 형태)
     */
    public boolean ping() {
        if (!hasExternalConfig()) return false;
        try {
            HttpHeaders headers = defaultHeaders();
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("text", "ping");
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("parts", Collections.singletonList(part));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(apiUrl, entity, Map.class);
            return true;
        } catch (Exception ex) {
            log.warn("AI ping 실패", ex);
            return false;
        }
    }

    /**
     * JD 전체를 AI에 보내 로드맵을 생성. 실패 시 Optional.empty().
     */
    public Optional<RoadmapResponse> generateRoadmap(String jdText, RoadmapRequest request) {
        if (!hasExternalConfig()) {
            return Optional.empty();
        }
        try {
            RoadmapResponse roadmap = generateRoadmapStrict(jdText, request);
            return Optional.ofNullable(roadmap);
        } catch (Exception ex) {
            logHttpError("AI 로드맵 생성 호출 실패, 기본 로직으로 대체", ex);
        }
        return Optional.empty();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("X-goog-api-key", apiKey);
        return headers;
    }

    /**
     * AI가 미션 목록을 반환하도록 호출 (다양한 응답 형태를 수용)
     */
    private List<String> callSummaries(List<String> topics) {
        HttpHeaders headers = defaultHeaders();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("instruction", MISSION_INSTRUCTION);
        payload.put("topics", topics);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Object raw = restTemplate.postForObject(apiUrl, entity, Object.class);

        // JSON 객체에서 missions 추출
        Map<?, ?> map = normalizeToMap(raw);
        List<String> missions = extractMissions(map);
        if (!missions.isEmpty()) return missions;

        // 문자열이면 줄 단위로 파싱
        if (raw instanceof String str) {
            List<String> lines = str.lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            if (!lines.isEmpty()) return lines;
        }

        return fallbackMissions(topics);
    }

    private List<String> extractMissions(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return Collections.emptyList();
        Object missionsObj = map.get("missions");
        if (missionsObj instanceof List<?> list) {
            List<String> missions = list.stream()
                    .filter(o -> o != null && !o.toString().isBlank())
                    .map(Object::toString)
                    .collect(Collectors.toList());
            if (!missions.isEmpty()) return missions;
        }
        Object content = map.get("content");
        if (content instanceof String s && !s.isBlank()) {
            return s.lines().map(String::trim).filter(line -> !line.isBlank()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<String> fallbackMissions(List<String> topics) {
        return topics.stream()
                .map(t -> "실습 미션: " + t + " 중심으로 구현")
                .collect(Collectors.toList());
    }

    /**
     * Gemini 표준 contents 포맷으로만 로드맵 생성 (폴백 없음).
     */
    public RoadmapResponse generateRoadmapStrict(String jdText, RoadmapRequest request) {
        HttpHeaders headers = defaultHeaders();
        String prompt = buildRoadmapPrompt(jdText, request);

        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", Collections.singletonList(part));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Object raw = restTemplate.postForObject(apiUrl, entity, Object.class);
        return parseRoadmap(raw, request);
    }

    private String buildRoadmapPrompt(String jdText, RoadmapRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert career coach. Given a job description (JD) and user inputs, return ONLY JSON (no markdown, no prose).\n");
        sb.append("Schema:\n");
        sb.append("{\"targetRole\":\"string\",\"totalWeeks\":number,\"dailyHours\":number,\"keywords\":[\"string\"],\"steps\":[{\"title\":\"string\",\"estimatedWeeks\":number,\"weeks\":[{\"weekNumber\":number,\"topics\":[\"string\"],\"mission\":\"string\",\"detail\":\"string\",\"checklist\":[\"string\"]}]}]}");
        sb.append("\nRequirements:\n");
        sb.append("- Provide steps sequentially. totalWeeks should roughly match steps' estimatedWeeks sum.\n");
        sb.append("- For EACH week, include: topics(핵심내용), mission(핵심 행동/산출물), detail(한국어 1~2문장 세부 설명), checklist(3~5개의 세부 학습/실습 항목, 한국어).\n");
        sb.append("- Keep keywords short. Do not include prose outside JSON. JSON must be valid.\n");
        sb.append("User inputs: durationMonths=").append(request.getDurationMonths())
                .append(", dailyHours=").append(request.getDailyHours())
                .append(", level=").append(request.getLevel());
        sb.append("\nJD:\n").append(jdText == null ? "" : jdText);
        sb.append("\nRespond with JSON only.");
        return sb.toString();
    }

    /**
     * 로드맵에 대해 대화형 조언을 반환
     */
    public String chatRoadmap(String message, RoadmapResponse roadmap) {
        if (!hasExternalConfig()) return "AI 설정이 없습니다.";
        try {
            HttpHeaders headers = defaultHeaders();
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("text", buildChatPrompt(message, roadmap));
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("parts", Collections.singletonList(part));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            Object raw = restTemplate.postForObject(apiUrl, entity, Object.class);
            // 1) 기본 content 필드
            if (raw instanceof Map<?, ?> map) {
                Object contentField = map.get("content");
                if (contentField instanceof String s && !s.isBlank()) return s;
            }
            Map<?, ?> parsed = normalizeToMap(raw);
            if (parsed != null) {
                // 2) content 필드
                Object parsedContent = parsed.get("content");
                if (parsedContent instanceof String s && !s.isBlank()) return s;
                // 3) candidates → content.parts[].text
                Object candidates = parsed.get("candidates");
                if (candidates instanceof java.util.List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> cMap) {
                        Object cont = cMap.get("content");
                        if (cont instanceof Map<?, ?> cc) {
                            Object parts = cc.get("parts");
                            if (parts instanceof java.util.List<?> pList && !pList.isEmpty()) {
                                for (Object p : pList) {
                                    if (p instanceof Map<?, ?> pm) {
                                        Object text = pm.get("text");
                                        if (text instanceof String ts && !ts.isBlank()) return ts;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return "AI 응답이 비었습니다.";
        } catch (Exception ex) {
            logHttpError("AI chat 실패", ex);
            return "AI 응답에 실패했습니다.";
        }
    }

    private String buildChatPrompt(String message, RoadmapResponse roadmap) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful career coach. User message: ").append(message).append("\n");
        sb.append("Here is the roadmap JSON with current progress if provided:\n");
        sb.append(objectToJsonSafe(roadmap)).append("\n");
        sb.append("If progress information is present, tailor guidance accordingly.\n");
        sb.append("Respond in Korean, concise and actionable. Return plain text (no JSON). Provide at least 2 sentences.");
        return sb.toString();
    }

    private String objectToJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj == null ? "" : obj.toString();
        }
    }

    private void logHttpError(String message, Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            HttpStatusCodeException httpEx = (HttpStatusCodeException) ex;
            log.warn("{} (status: {}, body: {})", message, httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
        } else {
            log.warn(message, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private RoadmapResponse parseRoadmap(Object rawResponse, RoadmapRequest request) {
        Map<?, ?> response = normalizeToMap(rawResponse);
        if (response == null || response.isEmpty()) return null;

        String targetRole = asString(response.get("targetRole"), "AI 기반 로드맵");
        int totalWeeks = asInt(response.get("totalWeeks"), Math.max(4, request.getDurationMonths() * 4));
        int dailyHours = asInt(response.get("dailyHours"), request.getDailyHours());
        List<String> keywords = new ArrayList<>(asStringList(response.get("keywords")));

        List<RoadmapStep> steps = new ArrayList<>();
        Object stepsObj = response.get("steps");
        if (stepsObj instanceof List<?> list) {
            for (Object s : list) {
                if (s instanceof Map<?, ?>) {
                    Map<?, ?> stepMap = (Map<?, ?>) s;
                    steps.add(parseStep(stepMap, request));
                }
            }
        }

        // totalWeeks 보정
        if (totalWeeks <= 0) {
            totalWeeks = steps.stream().mapToInt(RoadmapStep::getEstimatedWeeks).sum();
            if (totalWeeks <= 0) {
                totalWeeks = Math.max(4, request.getDurationMonths() * 4);
            }
        }

        // dailyHours 보정
        if (dailyHours <= 0) {
            dailyHours = request.getDailyHours();
        }

        RoadmapResponse roadmap = new RoadmapResponse(targetRole, totalWeeks, dailyHours, keywords, steps);
        return roadmap;
    }

    private RoadmapStep parseStep(Map<?, ?> stepMap, RoadmapRequest request) {
        String title = asString(stepMap.get("title"), "Step");
        int estimatedWeeks = asInt(stepMap.get("estimatedWeeks"), 1);
        List<WeekPlan> weeks = new ArrayList<>();
        Object weeksObj = stepMap.get("weeks");
        if (weeksObj instanceof List<?> wList) {
            for (Object w : wList) {
                if (w instanceof Map<?, ?> wMap) {
                    weeks.add(parseWeek(wMap, weeks.size() + 1));
                }
            }
        }
        // weeks가 비어 있고 estimatedWeeks > 0이면 더미 미션으로 채워서 UI가 깨지지 않도록
        if (weeks.isEmpty() && estimatedWeeks > 0) {
            weeks.add(new WeekPlan(1, Collections.emptyList(), "실습 미션 준비"));
        }
        return new RoadmapStep(title, estimatedWeeks, weeks);
    }

    private WeekPlan parseWeek(Map<?, ?> wMap, int defaultWeekNumber) {
        int weekNumber = asInt(wMap.get("weekNumber"), defaultWeekNumber);
        List<String> topics = new ArrayList<>(asStringList(wMap.get("topics")));
        String mission = asString(wMap.get("mission"), topics.isEmpty() ? "실습 미션 수행" : "실습 미션: " + topics.get(0));
        String detail = asString(wMap.get("detail"), "");
        WeekPlan wp = new WeekPlan(weekNumber, topics, mission);
        wp.setDetail(detail);
        List<String> checklist = new ArrayList<>(asStringList(wMap.get("checklist")));
        wp.setChecklist(checklist);
        return wp;
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> normalizeToMap(Object raw) {
        if (raw instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) raw;
            if (map.containsKey("steps") || map.containsKey("missions")) return map;
            Map<?, ?> parsed = parseFromCommonFields(map);
            if (parsed != null && !parsed.isEmpty()) return parsed;
            return map;
        }
        Map<?, ?> parsed = parseJsonString(raw);
        if (parsed != null) return parsed;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> parseFromCommonFields(Map<?, ?> map) {
        Object candidates = map.get("candidates");
        if (candidates instanceof List<?>) {
            List<?> list = (List<?>) candidates;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?>) {
                    Map<?, ?> cm = (Map<?, ?>) first;
                    Object content = cm.get("content");
                    Map<?, ?> parsed = parseJsonString(content);
                    if (parsed != null) return parsed;
                    if (content instanceof Map<?, ?>) {
                        Map<?, ?> cMap = (Map<?, ?>) content;
                        Object parts = cMap.get("parts");
                        if (parts instanceof List<?> pList && !pList.isEmpty()) {
                            Object p0 = pList.get(0);
                            if (p0 instanceof Map<?, ?>) {
                                Map<?, ?> pMap = (Map<?, ?>) p0;
                                parsed = parseJsonString(pMap.get("text"));
                                if (parsed != null) return parsed;
                            }
                        }
                    }
                }
            }
        }
        Object choices = map.get("choices");
        if (choices instanceof List<?>) {
            List<?> list = (List<?>) choices;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?>) {
                    Map<?, ?> cm = (Map<?, ?>) first;
                    Object message = cm.get("message");
                    if (message instanceof Map<?, ?>) {
                        Map<?, ?> mm = (Map<?, ?>) message;
                        Object content = mm.get("content");
                        Map<?, ?> parsed = parseJsonString(content);
                        if (parsed != null) return parsed;
                        if (content instanceof Map<?, ?>) {
                            Map<?, ?> cMap = (Map<?, ?>) content;
                            Object parts = cMap.get("parts");
                            if (parts instanceof List<?> pList && !pList.isEmpty()) {
                                Object p0 = pList.get(0);
                                if (p0 instanceof Map<?, ?>) {
                                    Map<?, ?> pMap = (Map<?, ?>) p0;
                                    parsed = parseJsonString(pMap.get("text"));
                                    if (parsed != null) return parsed;
                                }
                            }
                        }
                    }
                    Map<?, ?> parsed = parseJsonString(cm.get("text"));
                    if (parsed != null) return parsed;
                }
            }
        }
        Map<?, ?> parsed = parseJsonString(map.get("content"));
        if (parsed != null) return parsed;
        parsed = parseJsonString(map.get("output"));
        if (parsed != null) return parsed;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> parseJsonString(Object candidate) {
        if (!(candidate instanceof String str) || str.isBlank()) return null;
        String trimmed = stripCodeFences(str.trim());
        try {
            return objectMapper.readValue(trimmed, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String stripCodeFences(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return text.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return text;
    }

    private String asString(Object obj, String def) {
        return obj == null ? def : obj.toString();
    }

    private int asInt(Object obj, int def) {
        if (obj instanceof Number n) return n.intValue();
        try {
            return obj == null ? def : Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private List<String> asStringList(Object obj) {
        if (!(obj instanceof List<?> list)) return Collections.emptyList();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Object o : list) {
            if (o != null) set.add(o.toString());
        }
        return new ArrayList<>(set);
    }
}
