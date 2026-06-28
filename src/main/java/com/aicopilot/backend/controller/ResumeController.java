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

    // NEW ENDPOINT: Triggered only when clicking the 'Check ATS Score' button
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
            // 1. Extract text from the uploaded PDF
            String extractedText = resumeExtractionService.extractText(file);

            // 2. Call our new dedicated ATS method
            String atsJsonResult = resumeAnalyzerService.calculateAtsScore(extractedText, jobDescription);

            // 3. Return the ATS metrics directly to the frontend
            return ResponseEntity.ok(atsJsonResult);

        } catch (Exception e) {
            e.printStackTrace();
            // Return a 503 status code with the exact error message we threw from the service
            return ResponseEntity.status(503).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping(value = "/export", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportToPdf(@RequestBody Map<String, Object> profileData) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateAtsResume(profileData);

            // Set the headers to tell the browser this is a downloadable file
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
            // 1. Extract text and Analyze with AI
            String extractedText = resumeExtractionService.extractText(file);
            String aiJsonResult = resumeAnalyzerService.analyzeResumeWithAI(extractedText);

            // FIX 1: Clean the AI response to remove any markdown formatting BEFORE parsing
            String cleanedJson = aiJsonResult.replace("```json", "").replace("```", "").trim();

            // 2. Identify the user who made the request (From the JWT Token)
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // 3. Find the user in MongoDB and save the AI data permanently
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Convert the cleanly formatted JSON string into a Map so MongoDB can store it cleanly
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> profileMap = mapper.readValue(cleanedJson, Map.class);

                user.setAiProfileData(profileMap);
                userRepository.save(user);
                System.out.println("✅ AI Profile permanently saved to MongoDB for: " + currentUserEmail);
            }

            // 4. Send the cleaned data back to React to display on the screen
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cleanedJson);

        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            // FIX 2: Specifically catch Google's 503 Overloaded Error
            System.out.println("⚠️ Google AI API is overloaded. Retrying advised.");
            return ResponseEntity.status(503)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"The AI servers are currently experiencing high demand. Please wait a moment and try again.\"}");

        } catch (Exception e) {
            // Catch any other generic crashes
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Failed to parse or analyze the document.\"}");
        }
    }
}