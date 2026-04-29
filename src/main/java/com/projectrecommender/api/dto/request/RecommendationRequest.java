package com.projectrecommender.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Request DTO for generating project recommendations.
 */
@Data
public class RecommendationRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private String sessionId;

    private Integer maxResults;

    private List<String> preferredDomains;

    private String additionalContext;
}
