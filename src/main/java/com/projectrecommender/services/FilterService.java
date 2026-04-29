package com.projectrecommender.services;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements rule-based filtering for the agent pipeline.
 * Filters projects based on difficulty, domain alignment, and skill overlap.
 */
@Service
@Slf4j
public class FilterService {

    /**
     * Filter projects by student difficulty level.
     * Beginner students get BEGINNER projects.
     * Intermediate students get BEGINNER + INTERMEDIATE.
     * Advanced students get all.
     */
    public List<Project> filterByDifficulty(List<Project> projects, Student.ExperienceLevel level) {
        return projects.stream()
                .filter(p -> isDifficultyCompatible(p.getDifficulty(), level))
                .collect(Collectors.toList());
    }

    /**
     * Filter projects by domain interest.
     * If no interests provided, return all.
     */
    public List<Project> filterByDomain(List<Project> projects, List<String> domainInterests) {
        if (domainInterests == null || domainInterests.isEmpty()) return projects;

        List<String> lowerInterests = domainInterests.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return projects.stream()
                .filter(p -> p.getDomain() != null &&
                        lowerInterests.stream().anyMatch(i -> p.getDomain().toLowerCase().contains(i)))
                .collect(Collectors.toList());
    }

    /**
     * Filter projects that have at least 'minOverlapCount' matching skills.
     */
    public List<Project> filterBySkillOverlap(List<Project> projects, Set<String> studentSkills, int minOverlapCount) {
        if (studentSkills == null || studentSkills.isEmpty()) return projects;

        return projects.stream()
                .filter(p -> {
                    long overlap = countSkillOverlap(p.getSkillsRequired(), studentSkills);
                    return overlap >= minOverlapCount;
                })
                .collect(Collectors.toList());
    }

    /**
     * Compute skill match score between project required skills and student skills.
     * Returns a value between 0.0 and 1.0.
     */
    public double computeSkillMatchScore(Project project, Set<String> studentSkills) {
        if (project.getSkillsRequired() == null || project.getSkillsRequired().isBlank()) return 0.0;

        List<String> required = parseSkillList(project.getSkillsRequired());
        if (required.isEmpty()) return 0.0;

        long matched = required.stream()
                .filter(r -> studentSkills.stream()
                        .anyMatch(s -> s.equalsIgnoreCase(r.trim())))
                .count();

        return (double) matched / required.size();
    }

    /**
     * Identify skill gaps: skills required by project that student doesn't have.
     */
    public List<String> identifySkillGaps(Project project, Set<String> studentSkills) {
        if (project.getSkillsRequired() == null || project.getSkillsRequired().isBlank()) {
            return Collections.emptyList();
        }

        List<String> required = parseSkillList(project.getSkillsRequired());
        return required.stream()
                .filter(r -> studentSkills.stream().noneMatch(s -> s.equalsIgnoreCase(r.trim())))
                .collect(Collectors.toList());
    }

    /**
     * Apply all rule-based filters in the correct order.
     */
    public List<Project> applyAllFilters(List<Project> projects,
                                          Student student,
                                          Set<String> studentSkillNames,
                                          List<String> domainInterests) {
        List<Project> filtered = projects;

        // Step 1: Filter by difficulty
        if (student.getExperienceLevel() != null) {
            filtered = filterByDifficulty(filtered, student.getExperienceLevel());
        }

        // Step 2: Filter by domain
        if (domainInterests != null && !domainInterests.isEmpty()) {
            List<Project> domainFiltered = filterByDomain(filtered, domainInterests);
            // If domain filtering removes too many, keep original
            if (domainFiltered.size() >= 5) {
                filtered = domainFiltered;
            }
        }

        log.debug("After rule-based filtering: {} projects remain.", filtered.size());
        return filtered;
    }

    // ==================== Private Helpers ====================

    private boolean isDifficultyCompatible(Project.Difficulty projectDifficulty, Student.ExperienceLevel level) {
        if (projectDifficulty == null || level == null) return true;
        return switch (level) {
            case BEGINNER -> projectDifficulty == Project.Difficulty.BEGINNER;
            case INTERMEDIATE -> projectDifficulty == Project.Difficulty.BEGINNER
                    || projectDifficulty == Project.Difficulty.INTERMEDIATE;
            case ADVANCED -> true;
        };
    }

    private long countSkillOverlap(String requiredSkillsStr, Set<String> studentSkills) {
        if (requiredSkillsStr == null || requiredSkillsStr.isBlank()) return 0;
        List<String> required = parseSkillList(requiredSkillsStr);
        return required.stream()
                .filter(r -> studentSkills.stream().anyMatch(s -> s.equalsIgnoreCase(r.trim())))
                .count();
    }

    private List<String> parseSkillList(String skillsStr) {
        if (skillsStr == null || skillsStr.isBlank()) return Collections.emptyList();
        return Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
