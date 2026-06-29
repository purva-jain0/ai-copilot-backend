package com.aicopilot.backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    /**
     * Sends the 6-digit OTP verification access code.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        Resend resend = new Resend(resendApiKey);

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                + "<h2 style='color: #2563eb;'>Welcome to AI Career Copilot</h2>"
                + "<p style='font-size: 16px; color: #333;'>Your one-time verification password is:</p>"
                + "<h1 style='color: #2563eb; letter-spacing: 2px; font-size: 32px; margin: 10px 0;'>" + otp + "</h1>"
                + "<p style='font-size: 14px; color: #666;'>This code will expire in 5 minutes. Do not share this code with anyone.</p>"
                + "</div>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("AI Copilot Team <onboarding@resend.dev>") // Free tier requires onboarding@resend.dev
                .to(toEmail)
                .subject("Your Career Copilot Verification Code")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("✅ OTP email sent successfully via Resend. ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("❌ Failed to send OTP email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Email dispatch failed: " + e.getMessage());
        }
    }

    /**
     * Sends the weekly dynamic learning reminder.
     */
    public void sendRoadmapReminder(String toEmail, String userName, int weekNumber, String focusTopic) {
        Resend resend = new Resend(resendApiKey);

        // NOTE: For production, you will change 'http://localhost:5173' to your live Vercel frontend URL!
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                + "<h2 style='color: #2563eb;'>AI Copilot Learning Tracker</h2>"
                + "<p style='font-size: 16px; color: #333;'>Hi <b>" + userName + "</b>,</p>"
                + "<p style='font-size: 16px; color: #333;'>It is time to crush Week " + weekNumber + " of your personalized career roadmap.</p>"
                + "<div style='background-color: #f3f4f6; padding: 15px; border-left: 4px solid #2563eb; margin: 20px 0;'>"
                + "<h3 style='margin: 0; color: #1e40af;'>This week's focus: " + focusTopic + "</h3>"
                + "</div>"
                + "<p style='font-size: 16px; color: #333;'>Log in to your dashboard to find your curated YouTube tutorials and action items.</p>"
                + "<a href='https://ai-copilot-frontend-sepia.vercel.app' style='display: inline-block; padding: 12px 24px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 10px;'>Go to Dashboard</a>"
                + "<p style='font-size: 12px; color: #888; margin-top: 30px;'>Keep pushing forward, you've got this!<br>— The AI Copilot Team</p>"
                + "</div>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("AI Copilot Team <onboarding@resend.dev>")
                .to(toEmail)
                .subject("🚀 Your AI Copilot Roadmap: Week " + weekNumber + " Starts Now!")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("✅ Reminder email sent successfully via Resend to: " + toEmail + ". ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("❌ Failed to send reminder email to " + toEmail + ": " + e.getMessage());
        }
    }
}