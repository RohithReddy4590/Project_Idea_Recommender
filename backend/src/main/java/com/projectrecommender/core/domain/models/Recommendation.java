package com.projectrecommender.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private Project project;
    private double score;
    private double skillMatchScore;
    private double semanticScore;
    private double llmScore;
    private String reason;
    private List<String> skillGap;
}
