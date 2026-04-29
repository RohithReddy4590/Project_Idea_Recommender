package com.projectrecommender.agent.reasoning;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.service.SkillTaxonomyService;
import com.projectrecommender.services.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Identifies missing skills and provides learning path recommendations.
 * Part of the agent's reasoning layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SkillGapIdentifier {

    private final FilterService filterService;
    private final SkillTaxonomyService skillTaxonomyService;

    /**
     * Identify skill gaps between a project's required skills and student skills.
     *
     * @param project       Project entity
     * @param studentSkills Set of student skill names
     * @return              SkillGapResult with gaps and learning path
     */
    public SkillGapResult identify(Project project, Set<String> studentSkills) {
        List<String> gaps = filterService.identifySkillGaps(project, studentSkills);
        List<String> learningPath = buildLearningPath(gaps);
        double readinessScore = computeReadinessScore(project, studentSkills, gaps);

        return new SkillGapResult(gaps, learningPath, readinessScore);
    }

    /**
     * Build an ordered learning path for missing skills.
     * Tries to order prerequisites before advanced skills.
     */
    private List<String> buildLearningPath(List<String> gaps) {
        if (gaps.isEmpty()) return Collections.emptyList();

        // Separate beginner-level gaps from advanced ones using taxonomy
        List<String> beginnerFirst = new ArrayList<>();
        List<String> advancedLater = new ArrayList<>();

        for (String gap : gaps) {
            skillTaxonomyService.findSkillByName(gap).ifPresentOrElse(skill -> {
                if (skill.getDifficultyLevel() != null && skill.getDifficultyLevel() <= 2) {
                    beginnerFirst.add(gap);
                } else {
                    advancedLater.add(gap);
                }
            }, () -> beginnerFirst.add(gap)); // Unknown skills go first
        }

        List<String> path = new ArrayList<>(beginnerFirst);
        path.addAll(advancedLater);
        return path;
    }

    /**
     * Compute a readiness score: 1.0 = fully ready, 0.0 = no skills match.
     */
    private double computeReadinessScore(Project project, Set<String> studentSkills, List<String> gaps) {
        double skillMatch = filterService.computeSkillMatchScore(project, studentSkills);
        return skillMatch;
    }

    /**
     * Value object for skill gap analysis result.
     */
    public static class SkillGapResult {
        private final List<String> missingSkills;
        private final List<String> learningPath;
        private final double readinessScore;

        public SkillGapResult(List<String> missingSkills, List<String> learningPath, double readinessScore) {
            this.missingSkills = missingSkills;
            this.learningPath = learningPath;
            this.readinessScore = readinessScore;
        }

        public List<String> getMissingSkills() { return missingSkills; }
        public List<String> getLearningPath() { return learningPath; }
        public double getReadinessScore() { return readinessScore; }
        public boolean isReadyNow() { return missingSkills.isEmpty(); }
    }
}
