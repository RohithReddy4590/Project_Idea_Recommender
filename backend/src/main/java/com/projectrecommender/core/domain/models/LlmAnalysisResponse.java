package com.projectrecommender.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmAnalysisResponse {
    @Builder.Default
    private Map<Long, Double> scores = new HashMap<>();
    @Builder.Default
    private Map<Long, String> explanations = new HashMap<>();
    @Builder.Default
    private Map<Long, List<String>> skillGaps = new HashMap<>();
}
