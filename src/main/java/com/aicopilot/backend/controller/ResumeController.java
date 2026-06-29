package com.aicopilot.backend.controller;

import com.aicopilot.backend.model.User;
import com.aicopilot.backend.repository.UserRepository;
import com.aicopilot.backend.service.PdfGenerationService;
import com.aicopilot.backend.service.ResumeExtractionService;
import com.aicopilot.backend.service.ResumeAnalyzerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = {
        "https://ai-copilot-frontend-eei4v55by-purvajain606-1810s-projects.vercel.app",
        "https://ai-copilot-frontend-sepia.vercel.app"
})
public class ResumeController {

    @Autowired
    private ResumeExtractionService resumeExtractionService;

    @Autowired
    private ResumeAnalyzerService resumeAnalyzerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);

            if (userOptional.isPresent() && userOptional.get().getAiProfileData() != null) {
                return ResponseEntity.ok(userOptional.get().getAiProfileData());
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to fetch profile data.\"}");
        }
    }

    @PostMapping("/check-ats")
    public ResponseEntity<?> checkAtsScore(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Please upload a resume file.\"}");
        }
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Job description cannot be empty.\"}");
        }

        try {
            String extractedText = resumeExtractionService.extractText(file);
            String atsJsonResult = resumeAnalyzerService.calculateAtsScore(extractedText, jobDescription);
            return ResponseEntity.ok(atsJsonResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping(value = "/export", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportToPdf(@RequestBody Map<String, Object> profileData) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateAtsResume(profileData);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Optimized_ATS_Resume.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file.");
        }

        try {
            String extractedText = resumeExtractionService.extractText(file);
            String aiJsonResult = resumeAnalyzerService.analyzeResumeWithAI(extractedText);

            String cleanedJson = aiJsonResult.replace("```json", "").replace("```", "").trim();
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> profileMap = mapper.readValue(cleanedJson, Map.class);

                user.setAiProfileData(profileMap);
                userRepository.save(user);
                System.out.println("✅ AI Profile permanently saved to MongoDB for: " + currentUserEmail);
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cleanedJson);

        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            System.out.println("⚠️ Google AI API is overloaded. Retrying advised.");
            return ResponseEntity.status(503)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"The AI servers are currently experiencing high demand. Please wait a moment and try again.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Failed to parse or analyze the document.\"}");
        }
    }
}