package com.projectrecommender.agent.reasoning;

import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import com.projectrecommender.services.FilterService;
import com.projectrecommender.knowledgebase.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matches student profiles to relevant projects using skill overlap and semantic similarity.
 * Part of the agent's reasoning layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectMatcher {

    private final ProjectRepository projectRepository;
    private final FilterService filterService;
    private final VectorIndexService vectorIndexService;

    /**
     * Match projects for a student profile using hybrid approach:
     * 1. Rule-based filtering
     * 2. Semantic vector search
     * 3. Skill overlap scoring
     *
     * @param analysis  Analyzed student profile
     * @param student   Student entity (for experience level)
     * @param topK      Number of final candidates to return
     * @return          MatchResult containing matched projects and scores
     */
    public MatchResult matchProjects(ProfileAnalysis analysis, Student student, int topK) {
        log.info("Matching projects for student: {}", analysis.getName());

        // Step 1: Get all projects
        List<Project> allProjects = projectRepository.findAll();

        // Step 2: Apply rule-based filters
        List<Project> filtered = filterService.applyAllFilters(
                allProjects, student, analysis.getSkillNames(), analysis.getDomainInterests());

        // Step 3: Compute skill match scores
        Map<Long, Double> skillScores = new HashMap<>();
        for (Project p : filtered) {
            double score = filterService.computeSkillMatchScore(p, analysis.getSkillNames());
            skillScores.put(p.getId(), score);
        }

        // Step 4: Semantic search for top-K candidates
        String queryText = buildSemanticQuery(analysis);
        List<Map.Entry<Long, Double>> semanticResults = vectorIndexService.searchSimilar(queryText, topK * 2);

        // Map semantic scores
        Map<Long, Double> semanticScores = new HashMap<>();
        for (Map.Entry<Long, Double> entry : semanticResults) {
            semanticScores.put(entry.getKey(), entry.getValue());
        }

        // Step 5: Merge filtered projects with semantic results
        Set<Long> filteredIds = filtered.stream().map(Project::getId).collect(Collectors.toSet());
        Set<Long> semanticIds = semanticScores.keySet();

        // Union of both sets
        Set<Long> candidateIds = new HashSet<>();
        candidateIds.addAll(filteredIds);
        candidateIds.addAll(semanticIds);

        // Fetch all candidate projects
        List<Project> candidates = projectRepository.findAllById(new ArrayList<>(candidateIds));

        // Fill missing skill scores
        for (Project p : candidates) {
            skillScores.putIfAbsent(p.getId(),
                    filterService.computeSkillMatchScore(p, analysis.getSkillNames()));
            semanticScores.putIfAbsent(p.getId(), 0.0);
        }

        log.info("ProjectMatcher: {} candidates after hybrid matching.", candidates.size());
        return new MatchResult(candidates, skillScores, semanticScores);
    }

    /**
     * Build a semantic query from the student profile for vector search.
     */
    private String buildSemanticQuery(ProfileAnalysis analysis) {
        return String.format(
            "Projects for a %s developer with skills in %s interested in %s aiming to become a %s",
            analysis.getSkillLevel(),
            String.join(", ", analysis.getSkillNames()),
            analysis.getDomainFocus(),
            analysis.getLlmCareerInsight()
        );
    }

    /**
     * Holds all matched projects with their individual dimension scores.
     */
    public static class MatchResult {
        private final List<Project> projects;
        private final Map<Long, Double> skillScores;
        private final Map<Long, Double> semanticScores;

        public MatchResult(List<Project> projects,
                            Map<Long, Double> skillScores,
                            Map<Long, Double> semanticScores) {
            this.projects = projects;
            this.skillScores = skillScores;
            this.semanticScores = semanticScores;
        }

        public List<Project> getProjects() { return projects; }
        public Map<Long, Double> getSkillScores() { return skillScores; }
        public Map<Long, Double> getSemanticScores() { return semanticScores; }
    }
}
