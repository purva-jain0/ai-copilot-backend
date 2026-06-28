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
public class MockInterviewService {

    // Ensure this matches the property name in your application.properties!
    // (e.g., gemini.api.key=AIzaSyYourKeyHere...)
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    // Phase 1: Generate 5 specific questions
    public String generateQuestions(String topic) throws Exception {
        String prompt = "You are a strict Senior Technical Interviewer. Generate exactly 5 technical interview questions to test a candidate's knowledge on: " + topic + ". " +
                "The questions should range from basic concepts to advanced scenario-based problems. " +
                "Return EXACTLY a raw JSON array of strings with NO markdown and NO extra text.\n" +
                "Example Format:\n[\"Question 1?\", \"Question 2?\", \"Question 3?\", \"Question 4?\", \"Question 5?\"]";

        return callGeminiApi(prompt);
    }

    // Phase 2: Grade the answers
    public String gradeInterview(String topic, List<String> questions, List<String> answers) throws Exception {
        String prompt = "You are a strict Senior Technical Interviewer grading a candidate on: " + topic + ". " +
                "Evaluate their answers to the following 5 questions. Be critical but fair. " +
                "Calculate a final score out of 100. " +
                "Return EXACTLY a raw JSON object with NO markdown and NO extra text.\n" +
                "Use this exact schema:\n" +
                "{\n" +
                "  \"finalScore\": 85,\n" +
                "  \"feedbackSummary\": \"Overall thoughts on their performance\",\n" +
                "  \"detailedFeedback\": [\n" +
                "    {\"question\": \"Q1 text\", \"isCorrect\": true, \"feedback\": \"Why it was good or bad\"}\n" +
                "  ]\n" +
                "}\n\n" +
                "Questions: " + questions.toString() + "\n" +
                "Candidate Answers: " + answers.toString();

        return callGeminiApi(prompt);
    }

    // Helper method to construct the Gemini payload and handle errors
    private String callGeminiApi(String prompt) throws Exception {
        // Using Gemini 1.5 Flash for speed
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + geminiApiKey;

        // Constructing the Gemini-specific Payload
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", List.of(textPart));

        Map<String, Object> contentsMap = new HashMap<>();
        contentsMap.put("contents", List.of(partsMap));

        // Lower temperature to ensure strict JSON formatting
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("temperature", 0.2);
        contentsMap.put("generationConfig", configMap);

        String jsonPayload = mapper.writeValueAsString(contentsMap);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            String result = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return result.replace("```json", "").replace("```", "").trim();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Catches 400 Bad Request or 401 Unauthorized (e.g., bad API key)
            System.err.println("❌ GEMINI API REJECTED REQUEST: " + e.getResponseBodyAsString());
            throw new Exception("Gemini API Error: Check your API Key.");
        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            // Catches the classic 503 High Demand error
            System.err.println("❌ GEMINI API HIGH DEMAND (503)");
            throw new Exception("Google Gemini AI is currently experiencing high demand. Please try again in 60 seconds.");
        } catch (Exception e) {
            System.err.println("❌ INTERNAL SERVER ERROR: " + e.getMessage());
            throw new Exception("Failed to process AI response.");
        }
    }
}