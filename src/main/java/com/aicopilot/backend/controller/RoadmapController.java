package com.aicopilot.backend.controller;

import com.aicopilot.backend.model.User;
import com.aicopilot.backend.repository.UserRepository;
import com.aicopilot.backend.service.RoadmapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private UserRepository userRepository;

    // Endpoint 1: Generate the roadmap using existing AI Profile data
    @PostMapping("/generate")
    public ResponseEntity<?> generateAndSaveRoadmap() {
        try {
            // 1. Identify the user
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 2. Ensure they have uploaded a resume first
                if (user.getAiProfileData() == null) {
                    return ResponseEntity.badRequest().body("{\"error\": \"Please upload and analyze a resume first.\"}");
                }

                // 3. Convert their profile data back to a JSON string for the AI prompt
                ObjectMapper mapper = new ObjectMapper();
                String profileJson = mapper.writeValueAsString(user.getAiProfileData());

                // 4. Generate the Roadmap via Gemini AI
                System.out.println("Generating custom roadmap for: " + currentUserEmail + "...");
                String generatedRoadmapJson = roadmapService.generateRoadmap(profileJson);

                // 5. Save the Roadmap to MongoDB
                Map<String, Object> roadmapMap = mapper.readValue(generatedRoadmapJson, Map.class);
                user.setCareerRoadmap(roadmapMap);
                userRepository.save(user);

                System.out.println("✅ Roadmap successfully generated and saved!");
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(generatedRoadmapJson);
            }

            return ResponseEntity.status(404).body("{\"error\": \"User not found.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to generate roadmap.\"}");
        }
    }

    // Endpoint 2: Retrieve the saved roadmap on page refresh
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentRoadmap() {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);

            if (userOptional.isPresent() && userOptional.get().getCareerRoadmap() != null) {
                return ResponseEntity.ok(userOptional.get().getCareerRoadmap());
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch roadmap data.\"}");
        }
    }
}