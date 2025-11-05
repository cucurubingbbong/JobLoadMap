package com.jobloadmap.roadmap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkillGraphLoader {

    private final Map<String, SkillDefinition> skills = new HashMap<>();

    @PostConstruct
    public void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ClassPathResource resource = new ClassPathResource("skills.yml");
        try (InputStream is = resource.getInputStream()) {
            Map<String, Map<String, Object>> root = mapper.readValue(is, new TypeReference<>() {});
            Map<String, Map<String, Object>> rawSkills = (Map<String, Map<String, Object>>) root.get("skills");
            rawSkills.forEach((key, value) -> {
                SkillDefinition definition = mapper.convertValue(value, SkillDefinition.class);
                definition.setId(key);
                if (definition.getPrerequisites() == null) {
                    definition.setPrerequisites(List.of());
                }
                if (definition.getTopics() == null) {
                    definition.setTopics(List.of());
                }
                skills.put(key, definition);
            });
        }
    }

    public SkillDefinition getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, SkillDefinition> getSkills() {
        return skills;
    }
}
