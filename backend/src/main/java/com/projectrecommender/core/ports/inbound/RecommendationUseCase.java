package com.projectrecommender.core.ports.inbound;

import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;

public interface RecommendationUseCase {
    RecommendationResponse getRecommendations(RecommendationRequest request);
}
