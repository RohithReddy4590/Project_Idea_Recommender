package com.projectrecommender.infrastructure.adapters.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.core.ports.outbound.EmbeddingServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmbeddingAdapter implements EmbeddingServicePort {

    @org.springframework.beans.factory.annotation.Value("${gemini.api.key}")
    private String geminiApiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent?key=";

    @Override
    public float[] getEmbedding(String text) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return new float[768]; // Gemini embeddings are 768-dim
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", Map.of("parts", List.of(Map.of("text", text))));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(API_URL_BASE + geminiApiKey, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode values = root.path("embedding").path("values");
            
            float[] embedding = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = (float) values.get(i).asDouble();
            }
            return embedding;

        } catch (Exception e) {
            System.out.println("DEBUG: [Gemini-Embedding] Error: " + e.getMessage());
            return new float[768]; 
        }
    }
}
