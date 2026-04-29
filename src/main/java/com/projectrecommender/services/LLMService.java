package com.projectrecommender.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.config.OpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Service for interacting with OpenAI API.
 * Provides text generation and embedding capabilities for the AI agent.
 * Uses Java 11+ HttpClient for direct REST calls — no external SDK needed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;

    private static final String CHAT_URL  = "https://api.openai.com/v1/chat/completions";
    private static final String EMBED_URL = "https://api.openai.com/v1/embeddings";

    /**
     * Generate a text response from OpenAI chat model.
     *
     * @param systemPrompt  System-level instruction
     * @param userMessage   User message content
     * @return              Generated text response
     */
    public String generateText(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", openAIConfig.getModel());
            body.put("max_tokens", openAIConfig.getMaxTokens());
            body.put("temperature", openAIConfig.getTemperature());
            body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userMessage)
            ));

            String response = post(CHAT_URL, body);
            Map<?, ?> parsed  = objectMapper.readValue(response, Map.class);
            List<?>   choices = (List<?>) parsed.get("choices");
            if (choices == null || choices.isEmpty()) return "No response generated.";
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("LLM text generation failed: {}", e.getMessage());
            throw new com.projectrecommender.api.exception.AgentException("Unable to generate response at this time.", e);
        }
    }

    /**
     * Generate text embeddings for a given input string.
     *
     * @param text  Input text to embed
     * @return      List of floats representing the embedding vector
     */
    @SuppressWarnings("unchecked")
    public List<Float> generateEmbedding(String text) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", openAIConfig.getEmbeddingModel());
            body.put("input", text);

            String   response  = post(EMBED_URL, body);
            Map<?,?> parsed    = objectMapper.readValue(response, Map.class);
            List<?>  data      = (List<?>) parsed.get("data");
            if (data == null || data.isEmpty()) return Collections.emptyList();

            List<Double> raw    = (List<Double>) ((Map<?, ?>) data.get(0)).get("embedding");
            List<Float>  result = new ArrayList<>(raw.size());
            for (Double d : raw) result.add(d.floatValue());
            return result;

        } catch (Exception e) {
            log.error("Embedding generation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Ask the LLM to rank a set of projects for a given student profile.
     * Returns a JSON string: [{"projectId":1,"score":0.9,"reason":"..."}]
     *
     * @param profileSummary    Student profile plain-text summary
     * @param projectSummaries  Map of projectId -> one-line description
     * @return                  Raw JSON string from LLM
     */
    public String rankProjects(String profileSummary, Map<Long, String> projectSummaries) {
        String systemPrompt = loadPrompt("project_ranking_prompt.txt");

        StringBuilder sb = new StringBuilder();
        sb.append("Student Profile:\n").append(profileSummary).append("\n\nProjects to Rank:\n");
        projectSummaries.forEach((id, desc) ->
            sb.append("ID ").append(id).append(": ").append(desc).append("\n")
        );
        sb.append("\nRespond ONLY with a valid JSON array like: ")
          .append("[{\"projectId\": 1, \"score\": 0.9, \"reason\": \"...\"}]");

        return generateText(systemPrompt, sb.toString());
    }

    /**
     * Generate a human-readable explanation for a recommended project.
     *
     * @param profileSummary  Student profile summary
     * @param projectTitle    Title of the project
     * @param projectDesc     Project description
     * @param skillGaps       List of missing skills
     * @return                Explanation string
     */
    public String generateExplanation(String profileSummary,
                                       String projectTitle,
                                       String projectDesc,
                                       List<String> skillGaps) {
        String systemPrompt = loadPrompt("explanation_prompt.txt");
        String userMessage  = String.format(
            "Student Profile: %s\n\nProject: %s\nDescription: %s\nSkill Gaps: %s\n\n"
            + "Generate a concise, motivating explanation.",
            profileSummary, projectTitle, projectDesc,
            skillGaps.isEmpty() ? "None — student is fully ready!" : String.join(", ", skillGaps)
        );
        return generateText(systemPrompt, userMessage);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Load a prompt file from classpath resources/prompts/.
     */
    private String loadPrompt(String filename) {
        try {
            var stream = getClass().getClassLoader()
                                   .getResourceAsStream("prompts/" + filename);
            if (stream == null) {
                log.warn("Prompt file not found: {}", filename);
                return "You are a helpful AI assistant.";
            }
            return new String(stream.readAllBytes()).trim();
        } catch (Exception e) {
            log.warn("Could not load prompt file {}: {}", filename, e.getMessage());
            return "You are a helpful AI assistant.";
        }
    }

    /**
     * HTTP POST to an OpenAI endpoint with JSON body.
     */
    private String post(String url, Map<String, Object> body) throws Exception {
        String json = objectMapper.writeValueAsString(body);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(openAIConfig.getTimeout()))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type",  "application/json")
            .header("Authorization", "Bearer " + openAIConfig.getApiKey())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .timeout(Duration.ofSeconds(openAIConfig.getTimeout()))
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("OpenAI API error {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API returned HTTP " + response.statusCode());
        }
        return response.body();
    }
}
