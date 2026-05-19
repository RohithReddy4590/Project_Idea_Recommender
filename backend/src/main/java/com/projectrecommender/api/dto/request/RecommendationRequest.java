package com.projectrecommender.api.dto.request;

import com.projectrecommender.core.domain.enums.ExperienceLevel;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationRequest {
    private List<String> skills;
    private List<String> interests;
    private String goal;
    private ExperienceLevel experienceLevel;
}
