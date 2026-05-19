package com.projectrecommender.infrastructure.services;

import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import com.projectrecommender.core.ports.outbound.EmbeddingServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SemanticMatchingService {

    private final EmbeddingServicePort embeddingService;

    public Map<Long, Double> calculateSimilarities(List<Project> projects, Student student) {
        String studentText = student.getCareerGoal() + " " + String.join(" ", student.getInterests());
        float[] studentEmbedding = embeddingService.getEmbedding(studentText);

        Map<Long, Double> scores = new HashMap<>();
        for (Project p : projects) {
            double semantic = 0.0;
            if (!isAllZeros(studentEmbedding)) {
                String projectText = p.getTitle() + " " + p.getDescription();
                float[] projectEmbedding = embeddingService.getEmbedding(projectText);
                semantic = cosineSimilarity(studentEmbedding, projectEmbedding);
            }
            
            // Keyword fallback (Jaccard-ish)
            double keywordMatch = calculateKeywordMatch(p, student);
            
            // Combine them: 70% semantic (AI), 30% keywords (Backup)
            // If AI failed (0.0), keywords take over more weight
            double finalScore = (semantic > 0) ? (0.7 * semantic + 0.3 * keywordMatch) : keywordMatch;
            
            scores.put(p.getId(), finalScore);
        }
        return scores;
    }

    private double calculateKeywordMatch(Project p, Student s) {
        Set<String> studentTerms = new HashSet<>();
        studentTerms.addAll(Arrays.asList(s.getCareerGoal().toLowerCase().split("\\s+")));
        s.getSkills().forEach(sk -> studentTerms.add(sk.toLowerCase()));
        
        String projectTerms = (p.getTitle() + " " + p.getDescription() + " " + String.join(" ", p.getTechStack())).toLowerCase();
        
        long matched = studentTerms.stream()
                .filter(term -> term.length() > 2 && projectTerms.contains(term))
                .count();
                
        return studentTerms.isEmpty() ? 0.0 : (double) matched / studentTerms.size();
    }

    private boolean isAllZeros(float[] vector) {
        for (float v : vector) {
            if (v != 0.0f) return false;
        }
        return true;
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length || vectorA.length == 0) return 0.0;
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
