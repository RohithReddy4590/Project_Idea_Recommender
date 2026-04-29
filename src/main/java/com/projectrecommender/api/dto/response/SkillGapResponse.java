package com.projectrecommender.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Response DTO for skill gap analysis of a specific project.
 */
@Data
@Builder
public class SkillGapResponse {

    private Long projectId;
    private String projectTitle;

    private List<String> missingSkills;
    private List<String> learningPath;
    private double readinessScore;
    private boolean readyNow;

    public int getReadinessPercentage() {
        return (int) Math.round(readinessScore * 100);
    }
}
