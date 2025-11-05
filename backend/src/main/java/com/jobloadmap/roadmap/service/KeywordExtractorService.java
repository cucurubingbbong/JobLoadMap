package com.jobloadmap.roadmap.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KeywordExtractorService {

    private final Map<String, String> keywordToSkillId = new HashMap<>();

    public KeywordExtractorService() {
        register("java", "java");
        register("spring boot", "spring-boot");
        register("spring", "spring");
        register("mysql", "mysql");
        register("sql", "sql");
        register("rest", "rest-api");
        register("api", "rest-api");
        register("docker", "docker");
        register("aws", "aws");
        register("redis", "redis");
        register("kafka", "kafka");
        register("junit", "junit");
        register("testing", "testing");
        register("git", "git-basics");
        register("http", "http");
        register("자료구조", "datastructures");
        register("알고리즘", "algorithms");
        register("javascript", "javascript");
        register("react", "react");
        register("프론트", "frontend-basics");
    }

    private void register(String keyword, String skillId) {
        keywordToSkillId.put(keyword.toLowerCase(), skillId);
    }

    public Set<String> extractSkillIds(String text) {
        Set<String> matches = new HashSet<>();
        String normalized = text.toLowerCase();
        for (Map.Entry<String, String> entry : keywordToSkillId.entrySet()) {
            String keyword = entry.getKey();
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                matches.add(entry.getValue());
            }
        }
        return matches;
    }

    public String fetchTextFromUrl(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "JobLoadMapBot/1.0")
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String body = new String(response.body(), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(body);
        String clean = Jsoup.clean(doc.body().html(), Safelist.none());
        return clean.replaceAll("\\s+", " ").trim();
    }
}
