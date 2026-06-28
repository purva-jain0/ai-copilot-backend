package com.aicopilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    public List<Map<String, String>> searchVideos(String query) {
        // We only want 3 highly relevant tutorial videos
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=3&q="
                + query + " tutorial&type=video&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, String>> videoList = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode items = root.path("items");
            for (JsonNode item : items) {
                Map<String, String> videoData = new HashMap<>();
                videoData.put("videoId", item.path("id").path("videoId").asText());
                videoData.put("title", item.path("snippet").path("title").asText());
                videoData.put("channelTitle", item.path("snippet").path("channelTitle").asText());
                videoData.put("thumbnail", item.path("snippet").path("thumbnails").path("medium").path("url").asText());
                videoList.add(videoData);
            }
            return videoList;

        } catch (Exception e) {
            System.err.println("⚠️ YouTube API Call Failed. Triggering Dev Bypass Videos...");

            // DEV BYPASS: If you haven't set up the API key yet, return these hardcoded real videos!
            Map<String, String> vid1 = new HashMap<>();
            vid1.put("videoId", "bMknfKXIFA8");
            vid1.put("title", "React Course - Beginner's Tutorial for React JavaScript Library");
            vid1.put("channelTitle", "freeCodeCamp.org");
            vid1.put("thumbnail", "https://i.ytimg.com/vi/bMknfKXIFA8/mqdefault.jpg");

            Map<String, String> vid2 = new HashMap<>();
            vid2.put("videoId", "9SGDpanrc8U");
            vid2.put("title", "Spring Boot Tutorial for Beginners");
            vid2.put("channelTitle", "Amigoscode");
            vid2.put("thumbnail", "https://i.ytimg.com/vi/9SGDpanrc8U/mqdefault.jpg");

            videoList.add(vid1);
            videoList.add(vid2);
            return videoList;
        }
    }
}