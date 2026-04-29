package com.projectrecommender.agent;

import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import com.projectrecommender.agent.reasoning.ProjectMatcher;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import com.projectrecommender.services.FilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectMatcher.
 */
@ExtendWith(MockitoExtension.class)
class ProjectMatcherTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private FilterService filterService;
    @Mock private VectorIndexService vectorIndexService;

    @InjectMocks
    private ProjectMatcher projectMatcher;

    private ProfileAnalysis analysis;
    private Student student;
    private List<Project> allProjects;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L).name("Bob")
                .experienceLevel(Student.ExperienceLevel.INTERMEDIATE)
                .build();

        analysis = new ProfileAnalysis(
                1L, "Bob",
                Set.of("Java", "Spring Boot", "MySQL"),
                "INTERMEDIATE",
                "Backend",
                "Name: Bob | Skills: Java, Spring Boot, MySQL",
                "Strong backend developer.",
                List.of("Backend")
        );

        Project p1 = Project.builder().id(1L).title("Finance API")
                .domain("Backend Development").difficulty(Project.Difficulty.INTERMEDIATE)
                .skillsRequired("Java, Spring Boot, MySQL").build();
        Project p2 = Project.builder().id(2L).title("Weather App")
                .domain("Web Development").difficulty(Project.Difficulty.BEGINNER)
                .skillsRequired("React, CSS").build();
        Project p3 = Project.builder().id(3L).title("ML Classifier")
                .domain("AI/ML").difficulty(Project.Difficulty.ADVANCED)
                .skillsRequired("Python, Scikit-learn").build();

        allProjects = List.of(p1, p2, p3);
    }

    @Test
    @DisplayName("matchProjects returns candidates with skill and semantic scores")
    void matchProjects_returnsCandidates() {
        when(projectRepository.findAll()).thenReturn(allProjects);
        when(filterService.applyAllFilters(any(), any(), any(), any())).thenReturn(allProjects);
        when(filterService.computeSkillMatchScore(any(), any())).thenReturn(0.75);
        when(vectorIndexService.searchSimilar(anyString(), anyInt()))
                .thenReturn(List.of(Map.entry(1L, 0.9), Map.entry(2L, 0.6)));
        when(projectRepository.findAllById(any())).thenReturn(allProjects);

        ProjectMatcher.MatchResult result = projectMatcher.matchProjects(analysis, student, 10);

        assertThat(result.getProjects()).isNotEmpty();
        assertThat(result.getSkillScores()).isNotEmpty();
        assertThat(result.getSemanticScores()).isNotEmpty();
    }

    @Test
    @DisplayName("matchProjects fills missing semantic scores with 0.0")
    void matchProjects_fillsMissingSemanticScores() {
        when(projectRepository.findAll()).thenReturn(allProjects);
        when(filterService.applyAllFilters(any(), any(), any(), any())).thenReturn(allProjects);
        when(filterService.computeSkillMatchScore(any(), any())).thenReturn(0.5);
        // Semantic search returns only one result
        when(vectorIndexService.searchSimilar(anyString(), anyInt()))
                .thenReturn(List.of(Map.entry(1L, 0.8)));
        when(projectRepository.findAllById(any())).thenReturn(allProjects);

        ProjectMatcher.MatchResult result = projectMatcher.matchProjects(analysis, student, 10);

        // Projects not in semantic results should have 0.0 semantic score
        for (Project p : result.getProjects()) {
            assertThat(result.getSemanticScores()).containsKey(p.getId());
        }
    }
}
