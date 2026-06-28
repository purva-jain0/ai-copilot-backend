package com.aicopilot.backend.controller;

import com.aicopilot.backend.model.User;
import com.aicopilot.backend.repository.UserRepository;
import com.aicopilot.backend.security.JwtUtil;
import com.aicopilot.backend.service.OtpService;
import com.aicopilot.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "ai-copilot-frontend-eei4v55by-purvajain606-1810s-projects.vercel.app")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email field is mandatory.");
        }

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of("message", "A 6-digit verification code has been dispatched to " + email));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        boolean isValid = otpService.validateOtp(email, otp);
        if (!isValid) {
            return ResponseEntity.status(401).body("Invalid or expired verification code.");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setProfileComplete(false);
            userRepository.save(user);
        } else {
            user = existingUser.get();
        }


        String token = jwtUtil.generateToken(email);

        return ResponseEntity.ok(Map.of(
                "message", "Verification successful",
                "email", email,
                "isProfileComplete", user.isProfileComplete(),
                "token", token // RETURN TOKEN TO FRONTEND
        ));
    }
    @PutMapping("/onboarding")
    public ResponseEntity<?> completeOnboarding(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String fullName = (String) request.get("fullName");
        String className = (String) request.get("className");
        String college = (String) request.get("college");
        String experienceLevel = (String) request.get("experienceLevel");

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User record not found matching the provided identity context.");
        }

        User user = userOptional.get();

        User.UserProfile profile = new User.UserProfile();
        profile.setFullName(fullName);
        profile.setClassName(className);
        profile.setCollege(college);
        profile.setExperienceLevel(experienceLevel);

        user.setProfile(profile);
        user.setProfileComplete(true);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Profile registered successfully.",
                "user", user
        ));
    }
}