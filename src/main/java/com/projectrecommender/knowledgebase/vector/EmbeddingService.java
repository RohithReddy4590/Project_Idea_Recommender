package com.projectrecommender.knowledgebase.vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Generates and manages text embeddings for projects.
 * Uses OpenAI embedding API to create vector representations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    /**
     * Generate embedding text representation for a project.
     */
    public String buildProjectEmbeddingText(Project project) {
        return String.format(
            "Title: %s. Description: %s. Domain: %s. Skills Required: %s. Tech Stack: %s. Learning Outcomes: %s",
            project.getTitle(),
            project.getDescription(),
            project.getDomain(),
            project.getSkillsRequired(),
            project.getTechStack(),
            project.getLearningOutcomes()
        );
    }

    /**
     * Generate embedding text for a student profile query.
     */
    public String buildStudentQueryText(String skills, String interests, String careerGoal) {
        return String.format(
            "Student skills: %s. Interests: %s. Career goal: %s",
            skills, interests, careerGoal
        );
    }

    /**
     * Parse stored embedding vector JSON string to double array.
     */
    public double[] parseEmbedding(String embeddingJson) {
        try {
            List<Double> values = objectMapper.readValue(embeddingJson, new TypeReference<>() {});
            return values.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            log.error("Failed to parse embedding vector: {}", e.getMessage());
            return new double[0];
        }
    }

    /**
     * Serialize embedding array to JSON string for storage.
     */
    public String serializeEmbedding(List<Float> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            log.error("Failed to serialize embedding: {}", e.getMessage());
            return "[]";
        }
    }
}
