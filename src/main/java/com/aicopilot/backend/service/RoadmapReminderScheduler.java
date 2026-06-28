package com.aicopilot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoadmapReminderScheduler {

    @Autowired
    private EmailService emailService;

    // --- PRODUCTION CRON JOB ---
    // This runs every Monday at 9:00 AM server time.
    // @Scheduled(cron = "0 0 9 * * MON")
    // public void runWeeklyReminders() {
    //    System.out.println("Initiating Weekly Email Blast...");
    //    // In production, you would fetch all users from MongoDB here
    //    // and loop through them to send personalized emails based on their progress.
    // }

    // --- DEVELOPMENT TEST CRON JOB ---
    // This runs exactly 10 seconds after your server starts, so you can test it immediately!
    @Scheduled(initialDelay = 10000, fixedDelay = 99999999)
    public void testEmailSend() {
        System.out.println("⏳ Dev Scheduler triggered! Sending test email...");

        // Put YOUR real email here to test it
        String testEmail = "your.email@example.com";

        emailService.sendRoadmapReminder(testEmail, "Test User", 2, "Microservices Architecture");
    }
}