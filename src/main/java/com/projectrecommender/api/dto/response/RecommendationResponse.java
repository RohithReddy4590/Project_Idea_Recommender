package com.projectrecommender.api.dto.response;

import com.projectrecommender.knowledgebase.entity.Project;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Response DTO for a single project recommendation.
 * Contains the project, scores, explanation, and skill gap info.
 */
@Data
@Builder
public class RecommendationResponse {

    private Project project;

    private double finalScore;
    private double skillMatchScore;
    private double semanticScore;
    private double llmScore;
    private double readinessScore;

    private String explanation;

    private List<String> skillGaps;
    private List<String> learningPath;

    private String sessionId;

    /** Convenience method: returns percentage match */
    public int getMatchPercentage() {
        return (int) Math.round(skillMatchScore * 100);
    }
}
