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
@CrossOrigin(origins = {
        "https://ai-copilot-frontend-eei4v55by-purvajain606-1810s-projects.vercel.app",
        "https://ai-copilot-frontend-sepia.vercel.app"
})
public class RoadmapController {

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateAndSaveRoadmap() {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.getAiProfileData() == null) {
                    return ResponseEntity.badRequest().body("{\"error\": \"Please upload and analyze a resume first.\"}");
                }

                ObjectMapper mapper = new ObjectMapper();
                String profileJson = mapper.writeValueAsString(user.getAiProfileData());

                System.out.println("Generating custom roadmap for: " + currentUserEmail + "...");
                String generatedRoadmapJson = roadmapService.generateRoadmap(profileJson);

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