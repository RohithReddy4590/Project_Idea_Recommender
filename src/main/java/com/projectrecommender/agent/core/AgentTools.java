package com.projectrecommender.agent.core;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import com.projectrecommender.services.FilterService;
import com.projectrecommender.services.RankingService;
import com.projectrecommender.services.RankingService.RankedProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tool functions available to the AI agent.
 * These are discrete, callable utilities the orchestrator uses during reasoning.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgentTools {

    private final ProjectRepository projectRepository;
    private final FilterService filterService;
    private final VectorIndexService vectorIndexService;
    private final RankingService rankingService;

    /**
     * Tool: Search projects semantically by query text.
     *
     * @param query  Natural language query
     * @param topK   Number of results
     * @return       List of matching projects
     */
    public List<Project> searchProjects(String query, int topK) {
        log.debug("[Tool] searchProjects: query='{}', topK={}", query, topK);
        List<Map.Entry<Long, Double>> results = vectorIndexService.searchSimilar(query, topK);
        List<Long> ids = results.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        return projectRepository.findAllById(ids);
    }

    /**
     * Tool: Filter projects by domain, difficulty, and required skills.
     *
     * @param projects      Candidate projects
     * @param domains       Domain interests
     * @param difficulty    Max difficulty level
     * @param studentSkills Student's current skills
     * @return              Filtered projects
     */
    public List<Project> filterProjects(List<Project> projects,
                                         List<String> domains,
                                         Project.Difficulty difficulty,
                                         Set<String> studentSkills) {
        log.debug("[Tool] filterProjects: {} candidates", projects.size());
        return projects.stream()
                .filter(p -> difficulty == null || isDifficultyCompatible(p.getDifficulty(), difficulty))
                .filter(p -> domains == null || domains.isEmpty() ||
                        domains.stream().anyMatch(d -> p.getDomain() != null &&
                                p.getDomain().toLowerCase().contains(d.toLowerCase())))
                .collect(Collectors.toList());
    }

    /**
     * Tool: Rank projects using the hybrid ranking strategy.
     *
     * @param projects        Projects to rank
     * @param skillScores     Skill match scores per project ID
     * @param semanticScores  Semantic similarity scores per project ID
     * @param profileSummary  Student profile summary string
     * @return                Ranked projects ordered by score
     */
    public List<RankedProject> rankProjects(List<Project> projects,
                                             Map<Long, Double> skillScores,
                                             Map<Long, Double> semanticScores,
                                             String profileSummary) {
        log.debug("[Tool] rankProjects: {} candidates", projects.size());
        return rankingService.rankProjects(projects, skillScores, semanticScores, profileSummary);
    }

    /**
     * Tool: Identify skill gaps for a project given student skills.
     */
    public List<String> identifySkillGaps(Project project, Set<String> studentSkills) {
        log.debug("[Tool] identifySkillGaps: project='{}'", project.getTitle());
        return filterService.identifySkillGaps(project, studentSkills);
    }

    /**
     * Tool: Compute skill match score for a project.
     */
    public double computeSkillMatch(Project project, Set<String> studentSkills) {
        return filterService.computeSkillMatchScore(project, studentSkills);
    }

    // Helper
    private boolean isDifficultyCompatible(Project.Difficulty projectDiff, Project.Difficulty maxDiff) {
        if (projectDiff == null) return true;
        return projectDiff.ordinal() <= maxDiff.ordinal();
    }
}
