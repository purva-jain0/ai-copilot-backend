package com.aicopilot.backend.controller;

import com.aicopilot.backend.service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
@CrossOrigin(origins = {
        "https://ai-copilot-frontend-eei4v55by-purvajain606-1810s-projects.vercel.app",
        "https://ai-copilot-frontend-sepia.vercel.app"
})
public class LearningController {

    @Autowired
    private YouTubeService youtubeService;

    @GetMapping("/videos")
    public ResponseEntity<?> getTutorials(@RequestParam String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Search topic is required.\"}");
        }

        try {
            System.out.println("Fetching YouTube tutorials for: " + topic);
            List<Map<String, String>> videos = youtubeService.searchVideos(topic);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch videos.\"}");
        }
    }
}