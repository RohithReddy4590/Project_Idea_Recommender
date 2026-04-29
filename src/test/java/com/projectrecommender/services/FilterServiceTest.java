package com.projectrecommender.services;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FilterService — rule-based filtering logic.
 */
class FilterServiceTest {

    private FilterService filterService;
    private List<Project> projects;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();

        projects = List.of(
            Project.builder().id(1L).title("Beginner Task Manager")
                .difficulty(Project.Difficulty.BEGINNER).domain("Web Development")
                .skillsRequired("React, CSS").build(),

            Project.builder().id(2L).title("Intermediate Finance API")
                .difficulty(Project.Difficulty.INTERMEDIATE).domain("Backend Development")
                .skillsRequired("Java, Spring Boot, MySQL").build(),

            Project.builder().id(3L).title("Advanced Distributed Cache")
                .difficulty(Project.Difficulty.ADVANCED).domain("System Design")
                .skillsRequired("Java, Distributed Systems, Redis").build(),

            Project.builder().id(4L).title("AI Resume Analyzer")
                .difficulty(Project.Difficulty.INTERMEDIATE).domain("AI/ML")
                .skillsRequired("Java, Spring Boot, OpenAI API").build()
        );
    }

    @Test
    @DisplayName("BEGINNER student only gets BEGINNER projects")
    void filterByDifficulty_beginner_onlyBeginnerProjects() {
        List<Project> result = filterService.filterByDifficulty(projects, Student.ExperienceLevel.BEGINNER);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Beginner Task Manager");
    }

    @Test
    @DisplayName("INTERMEDIATE student gets BEGINNER and INTERMEDIATE projects")
    void filterByDifficulty_intermediate_getsBeginnerAndIntermediate() {
        List<Project> result = filterService.filterByDifficulty(projects, Student.ExperienceLevel.INTERMEDIATE);
        assertThat(result).hasSize(3);
        result.forEach(p ->
            assertThat(p.getDifficulty()).isIn(Project.Difficulty.BEGINNER, Project.Difficulty.INTERMEDIATE));
    }

    @Test
    @DisplayName("ADVANCED student gets all projects")
    void filterByDifficulty_advanced_getsAll() {
        List<Project> result = filterService.filterByDifficulty(projects, Student.ExperienceLevel.ADVANCED);
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("Domain filter returns matching projects")
    void filterByDomain_returnsMatchingProjects() {
        List<Project> result = filterService.filterByDomain(projects, List.of("Backend", "AI"));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Project::getTitle)
            .containsExactlyInAnyOrder("Intermediate Finance API", "AI Resume Analyzer");
    }

    @Test
    @DisplayName("Domain filter with empty list returns all projects")
    void filterByDomain_emptyList_returnsAll() {
        List<Project> result = filterService.filterByDomain(projects, List.of());
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("computeSkillMatchScore returns 1.0 for perfect match")
    void computeSkillMatchScore_perfectMatch() {
        Project p = projects.get(1); // Java, Spring Boot, MySQL
        Set<String> skills = Set.of("Java", "Spring Boot", "MySQL");
        double score = filterService.computeSkillMatchScore(p, skills);
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("computeSkillMatchScore returns 0.0 for no match")
    void computeSkillMatchScore_noMatch() {
        Project p = projects.get(1); // Java, Spring Boot, MySQL
        Set<String> skills = Set.of("Python", "React", "Docker");
        double score = filterService.computeSkillMatchScore(p, skills);
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("computeSkillMatchScore returns partial score for partial match")
    void computeSkillMatchScore_partialMatch() {
        Project p = projects.get(1); // Java, Spring Boot, MySQL
        Set<String> skills = Set.of("Java");
        double score = filterService.computeSkillMatchScore(p, skills);
        assertThat(score).isGreaterThan(0.0).isLessThan(1.0);
    }

    @Test
    @DisplayName("identifySkillGaps returns missing skills correctly")
    void identifySkillGaps_returnsMissingSkills() {
        Project p = projects.get(1); // Java, Spring Boot, MySQL
        Set<String> skills = Set.of("Java"); // student only has Java
        List<String> gaps = filterService.identifySkillGaps(p, skills);
        assertThat(gaps).containsExactlyInAnyOrder("Spring Boot", "MySQL");
    }

    @Test
    @DisplayName("identifySkillGaps returns empty list when all skills match")
    void identifySkillGaps_empty_whenAllMatched() {
        Project p = projects.get(1);
        Set<String> skills = Set.of("Java", "Spring Boot", "MySQL");
        List<String> gaps = filterService.identifySkillGaps(p, skills);
        assertThat(gaps).isEmpty();
    }
}
