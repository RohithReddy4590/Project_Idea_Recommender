package com.projectrecommender.agent.reasoning;

import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import com.projectrecommender.agent.reasoning.SkillGapIdentifier.SkillGapResult;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates human-readable, personalized explanations for project recommendations.
 * Uses LLM to produce motivating and specific explanations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExplanationGenerator {

    private final LLMService llmService;

    /**
     * Generate a personalized explanation for why a project is recommended.
     *
     * @param project     The recommended project
     * @param analysis    Student profile analysis
     * @param gapResult   Skill gap analysis for this project
     * @param finalScore  Final recommendation score
     * @return            Human-readable explanation
     */
    public String generate(Project project, ProfileAnalysis analysis,
                            SkillGapResult gapResult, double finalScore) {
        log.debug("Generating explanation for project: {}", project.getTitle());
        return llmService.generateExplanation(
                analysis.getProfileSummary(),
                project.getTitle(),
                project.getDescription(),
                gapResult.getMissingSkills()
        );
    }

    /**
     * Generate a fallback explanation without LLM (for offline/testing scenarios).
     */
    public String generateFallback(Project project, ProfileAnalysis analysis, SkillGapResult gapResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("'%s' is recommended for you because ", project.getTitle()));

        if (gapResult.getReadinessScore() > 0.7) {
            sb.append("you already have most of the required skills. ");
        } else if (gapResult.getReadinessScore() > 0.3) {
            sb.append("it aligns with your current skills and will help you grow. ");
        } else {
            sb.append("it represents an excellent growth opportunity for your career goals. ");
        }

        if (!gapResult.getMissingSkills().isEmpty()) {
            sb.append(String.format("To complete it, you'll want to learn: %s.",
                    String.join(", ", gapResult.getMissingSkills())));
        } else {
            sb.append("You have all the skills needed to get started today!");
        }

        if (project.getLearningOutcomes() != null && !project.getLearningOutcomes().isBlank()) {
            sb.append(String.format(" Completing this project will help you: %s", project.getLearningOutcomes()));
        }

        return sb.toString();
    }
}
