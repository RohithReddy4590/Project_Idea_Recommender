package com.projectrecommender.infrastructure.services.impl;

import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Recommendation;
import com.projectrecommender.core.domain.models.Student;
import com.projectrecommender.core.ports.inbound.RecommendationUseCase;
import com.projectrecommender.core.ports.outbound.OpenAIServicePort;
import com.projectrecommender.core.ports.outbound.ProjectRepositoryPort;
import com.projectrecommender.infrastructure.services.HybridScoringService;
import com.projectrecommender.infrastructure.services.RuleBasedFilterService;
import com.projectrecommender.infrastructure.services.SemanticMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final RuleBasedFilterService filterService;
    private final SemanticMatchingService semanticService;
    private final HybridScoringService scoringService;
    private final OpenAIServicePort openAIService;

    @Override
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        Student student = Student.builder()
                .skills(request.getSkills())
                .interests(request.getInterests())
                .careerGoal(request.getGoal())
                .experienceLevel(request.getExperienceLevel())
                .build();

        // 1. Fetch all projects
        List<Project> allProjects = projectRepository.findAll();
        System.out.println("DEBUG: [System] Found " + allProjects.size() + " total projects in DB.");

        // 2. Rule-Based Filtering
        List<Project> filtered = filterService.filter(allProjects, student);
        System.out.println("DEBUG: [System] " + filtered.size() + " projects remaining after filtering.");

        // 3. Dynamic Project Generation from OpenAI
        List<Project> dynamicProjects = openAIService.generateDynamicProjects(student, 3);
        System.out.println("DEBUG: [System] Dynamic Projects Generated: " + dynamicProjects.size());
        filtered.addAll(dynamicProjects);

        // 4. Semantic Matching
        Map<Long, Double> semanticScores = semanticService.calculateSimilarities(filtered, student);
        dynamicProjects.forEach(p -> 
            System.out.println("DEBUG: [System] Dynamic Project '" + p.getTitle() + "' Semantic Score: " + semanticScores.getOrDefault(p.getId(), 0.0))
        );

        // 5. LLM Ranking + Explanation + Skill Gap
        // We take top 10 filtered by semantic score for LLM analysis to save costs/tokens
        List<Project> topForLlm = filtered.stream()
                .sorted((p1, p2) -> {
                    double s1 = semanticScores.getOrDefault(p1.getId(), 0.0);
                    double s2 = semanticScores.getOrDefault(p2.getId(), 0.0);
                    return Double.compare(s2, s1);
                })
                .limit(10)
                .collect(Collectors.toList());

        com.projectrecommender.core.domain.models.LlmAnalysisResponse analysis = openAIService.getDetailedAnalysis(student, topForLlm);
        Map<Long, Double> llmScores = analysis.getScores();
        Map<Long, String> explanations = analysis.getExplanations();
        Map<Long, List<String>> skillGaps = analysis.getSkillGaps();

        // 5. Hybrid Scoring
        List<Recommendation> recommendations = new ArrayList<>();
        for (Project p : topForLlm) {
            double skillMatch = scoringService.calculateSkillMatchScore(p, student);
            double semantic = semanticScores.getOrDefault(p.getId(), 0.0);
            double llm = llmScores.getOrDefault(p.getId(), 5.0);
            
            double finalScore = scoringService.calculateFinalScore(skillMatch, semantic, llm);

            recommendations.add(Recommendation.builder()
                    .project(p)
                    .score(Math.round(finalScore * 1000) / 10.0) // 0-100 scale
                    .skillMatchScore(skillMatch)
                    .semanticScore(semantic)
                    .llmScore(llm)
                    .reason(explanations.getOrDefault(p.getId(), "Recommended based on matching profile."))
                    .skillGap(skillGaps.getOrDefault(p.getId(), List.of()))
                    .build());
        }

        // 6. Ranking
        recommendations.sort(Comparator.comparingDouble(Recommendation::getScore).reversed());

        return RecommendationResponse.builder()
                .recommendations(recommendations)
                .build();
    }
}
