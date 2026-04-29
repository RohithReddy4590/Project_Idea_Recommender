package com.projectrecommender.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.knowledgebase.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combines skill-based, semantic, and LLM scores into final ranking.
 * Implements a weighted scoring strategy for hybrid recommendation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    @Value("${agent.ranking-weight-skill:0.4}")
    private double skillWeight;

    @Value("${agent.ranking-weight-semantic:0.3}")
    private double semanticWeight;

    @Value("${agent.ranking-weight-llm:0.3}")
    private double llmWeight;

    /**
     * Rank projects using a hybrid of rule-based scores, semantic scores, and LLM scores.
     *
     * @param projects        Candidate projects
     * @param skillScores     Map of projectId -> skill match score (0-1)
     * @param semanticScores  Map of projectId -> semantic similarity score (0-1)
     * @param profileSummary  Student profile for LLM ranking
     * @return                Ranked list of projects with combined scores
     */
    public List<RankedProject> rankProjects(List<Project> projects,
                                             Map<Long, Double> skillScores,
                                             Map<Long, Double> semanticScores,
                                             String profileSummary) {
        // Step 1: Get LLM scores
        Map<Long, Double> llmScores = getLLMScores(projects, profileSummary);

        // Step 2: Combine all scores into final score
        List<RankedProject> ranked = new ArrayList<>();
        for (Project project : projects) {
            double skill = skillScores.getOrDefault(project.getId(), 0.0);
            double semantic = semanticScores.getOrDefault(project.getId(), 0.0);
            double llm = llmScores.getOrDefault(project.getId(), 0.0);

            double finalScore = (skill * skillWeight) + (semantic * semanticWeight) + (llm * llmWeight);

            ranked.add(new RankedProject(project, finalScore, skill, semantic, llm));
        }

        // Step 3: Sort by final score descending
        ranked.sort(Comparator.comparingDouble(RankedProject::getFinalScore).reversed());
        return ranked;
    }

    /**
     * Get LLM-based scores for a list of projects.
     * Falls back to portfolio impact score if LLM fails.
     */
    private Map<Long, Double> getLLMScores(List<Project> projects, String profileSummary) {
        Map<Long, Double> scores = new HashMap<>();

        // Only send top 20 projects to LLM for cost efficiency
        List<Project> subset = projects.size() > 20 ? projects.subList(0, 20) : projects;

        Map<Long, String> summaries = new LinkedHashMap<>();
        for (Project p : subset) {
            summaries.put(p.getId(), String.format("%s | Domain: %s | Skills: %s | Difficulty: %s",
                    p.getTitle(), p.getDomain(), p.getSkillsRequired(), p.getDifficulty()));
        }

        try {
            String response = llmService.rankProjects(profileSummary, summaries);
            // Clean up potential markdown
            String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();
            List<Map<String, Object>> parsed = objectMapper.readValue(cleaned, new TypeReference<>() {});

            for (Map<String, Object> entry : parsed) {
                Object idObj = entry.get("projectId");
                Object scoreObj = entry.get("score");
                if (idObj != null && scoreObj != null) {
                    Long id = Long.valueOf(idObj.toString());
                    double score = Double.parseDouble(scoreObj.toString());
                    scores.put(id, Math.min(1.0, Math.max(0.0, score)));
                }
            }
        } catch (Exception e) {
            log.warn("LLM ranking failed, falling back to portfolio impact scores: {}", e.getMessage());
            for (Project p : subset) {
                scores.put(p.getId(),
                        p.getPortfolioImpactScore() != null ? p.getPortfolioImpactScore() / 10.0 : 0.5);
            }
        }

        // Projects not in subset get 0.5 default
        for (Project p : projects) {
            scores.putIfAbsent(p.getId(), 0.5);
        }

        return scores;
    }

    /**
     * Represents a project with its composite recommendation scores.
     */
    public static class RankedProject {
        private final Project project;
        private final double finalScore;
        private final double skillScore;
        private final double semanticScore;
        private final double llmScore;

        public RankedProject(Project project, double finalScore, double skillScore,
                              double semanticScore, double llmScore) {
            this.project = project;
            this.finalScore = finalScore;
            this.skillScore = skillScore;
            this.semanticScore = semanticScore;
            this.llmScore = llmScore;
        }

        public Project getProject() { return project; }
        public double getFinalScore() { return finalScore; }
        public double getSkillScore() { return skillScore; }
        public double getSemanticScore() { return semanticScore; }
        public double getLlmScore() { return llmScore; }
    }
}
