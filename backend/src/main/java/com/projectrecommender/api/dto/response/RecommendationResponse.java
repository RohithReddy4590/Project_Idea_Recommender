package com.projectrecommender.api.dto.response;

import com.projectrecommender.core.domain.models.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<Recommendation> recommendations;
}
