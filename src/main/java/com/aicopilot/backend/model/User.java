package com.aicopilot.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private boolean isProfileComplete = false;

    private UserProfile profile;
    private Object aiProfileData;
    public Object getAiProfileData() {
        return aiProfileData;
    }
    public void setAiProfileData(Object aiProfileData) {
        this.aiProfileData = aiProfileData;
    }

    private Object careerRoadmap;

    public Object getCareerRoadmap() {
        return careerRoadmap;
    }

    public void setCareerRoadmap(Object careerRoadmap) {
        this.careerRoadmap = careerRoadmap;
    }

    @Data
    public static class UserProfile {
        private String fullName;
        private String className; // e.g., "MCA 8th Sem"
        private String college;
        private String experienceLevel; // "BEGINNER", "INTERMEDIATE", "PRO"
    }
}