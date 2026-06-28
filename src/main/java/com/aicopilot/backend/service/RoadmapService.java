package com.aicopilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoadmapService {

    // Ensure your application.properties has gemini.api.key set!
    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateRoadmap(String userProfileJson) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;

        // The Prompt: Act as a Career Coach and return a structured 4-week plan
        String prompt = "You are an expert Senior Tech Career Coach. Based on the following extracted candidate profile, generate a highly structured 4-week learning roadmap to help them upskill for their recommended roles. " +
                "Focus on the missing skills or areas of improvement mentioned in their actionable feedback. " +
                "Return EXACTLY a raw JSON object with NO markdown, NO backticks, and NO extra text. " +
                "Use exactly this structure:\n" +
                "{\n" +
                "  \"targetRole\": \"The primary role they should aim for based on their profile\",\n" +
                "  \"weeks\": [\n" +
                "    {\n" +
                "      \"weekNumber\": 1,\n" +
                "      \"focus\": \"Core theme for the week (e.g., Master Spring Boot Microservices)\",\n" +
                "      \"topics\": [\"Specific topic 1\", \"Specific topic 2\", \"Specific topic 3\"]\n" +
                "    }\n" +
                "    // Generate exactly 4 weeks of content following this pattern\n" +
                "  ]\n" +
                "}\n\n" +
                "Candidate Profile Data:\n" + userProfileJson;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        Map<String, Object> partContainer = new HashMap<>();
        partContainer.put("parts", List.of(textPart));
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("contents", List.of(partContainer));

        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(requestBodyMap);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        JsonNode root = mapper.readTree(response.getBody());
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }
}