package com.projectrecommender.api.controllers;

import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.core.ports.inbound.RecommendationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationUseCase recommendationUseCase;

    @PostMapping
    public RecommendationResponse getRecommendations(@RequestBody RecommendationRequest request) {
        return recommendationUseCase.getRecommendations(request);
    }
}
