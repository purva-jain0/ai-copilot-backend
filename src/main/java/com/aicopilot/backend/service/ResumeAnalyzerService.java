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
public class ResumeAnalyzerService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String analyzeResumeWithAI(String rawResumeText) throws Exception {

        // 1. Using the correct stable 1.5-flash model
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;

        String prompt = "You are an expert Senior Tech Recruiter. Deeply analyze the following resume text. " +
                "Extract the data and return EXACTLY a raw JSON object with NO markdown, NO backticks, and NO extra text. " +
                "Use exactly this JSON structure: \n" +
                "{\n" +
                "  \"candidateName\": \"Full name if found, else 'Unknown'\",\n" +
                "  \"experienceLevel\": \"BEGINNER, INTERMEDIATE, or ADVANCED\",\n" +
                "  \"summary\": \"A detailed 3-sentence professional summary highlighting their strongest traits.\",\n" +
                "  \"skills\": {\n" +
                "    \"languages\": [\"Java\", \"Python\", etc],\n" +
                "    \"frameworks\": [\"Spring Boot\", \"React\", etc],\n" +
                "    \"databases\": [\"MongoDB\", \"MySQL\", etc],\n" +
                "    \"tools\": [\"Git\", \"Docker\", etc]\n" +
                "  },\n" +
                "  \"recommendedRoles\": [\"Top 3 job titles they should apply for based on this stack\"],\n" +
                "  \"actionableFeedback\": \"One specific sentence on what tech skill is missing or what they should learn next to improve this resume.\"\n" +
                "}\n\n" +
                "Resume Text:\n" + rawResumeText;

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

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            // Catch the specific 503 High Demand error
            throw new Exception("Google Gemini AI is currently experiencing global high demand. This is temporary—please wait 60 seconds and try again.");
        } catch (Exception e) {
            // Catch any other API failures
            throw new Exception("AI API Connection Failed. Please try again.");
        }
    }


    public String calculateAtsScore(String rawResumeText, String targetJobDescription) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;

        String prompt = "You are a corporate applicant tracking system (ATS). Analyze the provided Resume Text against the Target Job Description.\n\n" +
                "Calculate a strict, realistic ATS compatibility score out of 100.\n" +
                "Return EXACTLY a raw JSON object with NO markdown backticks, NO markdown formatting, and no conversational text. " +
                "The JSON must follow this exact schema:\n" +
                "{\n" +
                "  \"atsScore\": 78,\n" +
                "  \"matchedKeywords\": [\"Java\", \"Spring Boot\"],\n" +
                "  \"missingKeywords\": [\"Docker\", \"Kubernetes\"],\n" +
                "  \"improvementTips\": \"Add metric-driven metrics to your experience section.\"\n" +
                "}\n\n" +
                "Target Job Description:\n" + targetJobDescription + "\n\n" +
                "Resume Text:\n" + rawResumeText;

        // Construct standard payload for Gemini 1.5 Flash
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("text", prompt);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", List.of(textMap));

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("contents", List.of(partsMap));

        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(contentMap);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            String textResult = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // Safety sanitization to strip markdown tags if Gemini accidentally includes them
            return textResult.replace("```json", "").replace("```", "").trim();
        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            // Catch the specific 503 High Demand error
            throw new Exception("Google Gemini AI is currently experiencing global high demand. This is temporary—please wait 60 seconds and try again.");
        } catch (Exception e) {
            // Catch any other API failures
            throw new Exception("AI API Connection Failed. Please try again.");
        }
    }
}