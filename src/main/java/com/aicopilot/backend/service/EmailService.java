package com.aicopilot.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;



    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
       message.setTo(toEmail);
        message.setSubject("Your Career Copilot Verification Code");
        message.setText("Welcome to AI Career Copilot.\n\n" +
                "Your one-time verification password is: " + otp + "\n" +
                "This code will expire in 5 minutes. Do not share this code with anyone.");
        mailSender.send(message);

    }
    public void sendRoadmapReminder(String toEmail, String userName, int weekNumber, String focusTopic) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("purvajain606@gmail.com", "AI Copilot Team");

            helper.setTo(toEmail);
            helper.setSubject("🚀 Your AI Copilot Roadmap: Week " + weekNumber + " Starts Now!");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                    + "<h2 style='color: #2563eb;'>AI Copilot Learning Tracker</h2>"
                    + "<p style='font-size: 16px; color: #333;'>Hi <b>" + userName + "</b>,</p>"
                    + "<p style='font-size: 16px; color: #333;'>It is time to crush Week " + weekNumber + " of your personalized career roadmap.</p>"
                    + "<div style='background-color: #f3f4f6; padding: 15px; border-left: 4px solid #2563eb; margin: 20px 0;'>"
                    + "<h3 style='margin: 0; color: #1e40af;'>This week's focus: " + focusTopic + "</h3>"
                    + "</div>"
                    + "<p style='font-size: 16px; color: #333;'>Log in to your dashboard to find your curated YouTube tutorials and action items.</p>"
                    + "<a href='http://localhost:5173' style='display: inline-block; padding: 12px 24px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 10px;'>Go to Dashboard</a>"
                    + "<p style='font-size: 12px; color: #888; margin-top: 30px;'>Keep pushing forward, you've got this!<br>— The AI Copilot Team</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("✅ Reminder email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

}