package com.aicopilot.backend.controller;

import com.aicopilot.backend.service.MockInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin(origins = "ai-copilot-frontend-eei4v55by-purvajain606-1810s-projects.vercel.app")
public class InterviewController {

    @Autowired
    private MockInterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, String> payload) {
        try {
            String topic = payload.get("topic");
            String jsonQuestions = interviewService.generateQuestions(topic);
            return ResponseEntity.ok(jsonQuestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body("{\"error\": \"AI Interviewer unavailable. Please try again.\"}");
        }
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeInterview(@RequestBody Map<String, Object> payload) {
        try {
            String topic = (String) payload.get("topic");
            List<String> questions = (List<String>) payload.get("questions");
            List<String> answers = (List<String>) payload.get("answers");

            String jsonGrades = interviewService.gradeInterview(topic, questions, answers);
            return ResponseEntity.ok(jsonGrades);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body("{\"error\": \"AI Grading failed. Please try again.\"}");
        }
    }
}