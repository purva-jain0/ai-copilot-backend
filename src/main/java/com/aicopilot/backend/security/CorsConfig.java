package com. aicopilot. backend. security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins("https://ai-copilot-frontend-sepia.vercel.app") // Your exact Vercel URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow all standard methods, including OPTIONS (preflight)
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}