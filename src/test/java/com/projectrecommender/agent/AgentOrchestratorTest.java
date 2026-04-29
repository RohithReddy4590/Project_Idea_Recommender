package com.projectrecommender.agent;

import com.projectrecommender.agent.core.AgentOrchestrator;
import com.projectrecommender.agent.core.AgentTools;
import com.projectrecommender.agent.core.StateManager;
import com.projectrecommender.agent.memory.InteractionHistory;
import com.projectrecommender.agent.memory.ProfileMemory;
import com.projectrecommender.agent.reasoning.*;
import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.Skill;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.entity.StudentSkill;
import com.projectrecommender.services.LLMService;
import com.projectrecommender.services.RankingService;
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
 * Unit tests for AgentOrchestrator pipeline.
 */
@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock private ProfileAnalyzer profileAnalyzer;
    @Mock private ProjectMatcher projectMatcher;
    @Mock private SkillGapIdentifier skillGapIdentifier;
    @Mock private ExplanationGenerator explanationGenerator;
    @Mock private AgentTools agentTools;
    @Mock private StateManager stateManager;
    @Mock private ProfileMemory profileMemory;
    @Mock private InteractionHistory interactionHistory;
    @Mock private LLMService llmService;

    @InjectMocks
    private AgentOrchestrator orchestrator;

    private Student student;
    private Project project;
    private ProfileAnalyzer.ProfileAnalysis mockAnalysis;

    @BeforeEach
    void setUp() {
        Skill javaSkill = Skill.builder().id(1L).name("Java").category("Programming").build();
        StudentSkill ss = StudentSkill.builder().skill(javaSkill)
                .proficiencyLevel(StudentSkill.ProficiencyLevel.INTERMEDIATE).build();

        student = Student.builder()
                .id(1L).name("Alice").email("alice@test.com")
                .careerGoal("Software Engineer")
                .experienceLevel(Student.ExperienceLevel.INTERMEDIATE)
                .studentSkills(Set.of(ss))
                .build();

        project = Project.builder()
                .id(10L).title("Finance Tracker API")
                .description("Build a REST API for tracking expenses.")
                .domain("Backend Development")
                .difficulty(Project.Difficulty.INTERMEDIATE)
                .skillsRequired("Java, Spring Boot, MySQL")
                .build();

        mockAnalysis = new ProfileAnalyzer.ProfileAnalysis(
                1L, "Alice", Set.of("Java"),
                "INTERMEDIATE", "Backend",
                "Name: Alice | Skills: Java | Level: INTERMEDIATE",
                "Strong Java developer aimed at backend engineering.",
                List.of("Backend")
        );

        // Mock StateManager to avoid NPE
        StateManager.AgentState mockState = new StateManager.AgentState("test-session");
        when(stateManager.getOrCreateState(anyString())).thenReturn(mockState);
    }

    @Test
    @DisplayName("Pipeline returns recommendations for valid student profile")
    void runPipeline_returnsRecommendations() {
        // Arrange
        RankingService.RankedProject rankedProject =
                new RankingService.RankedProject(project, 0.85, 0.9, 0.8, 0.85);

        ProjectMatcher.MatchResult matchResult = new ProjectMatcher.MatchResult(
                List.of(project),
                Map.of(10L, 0.9),
                Map.of(10L, 0.8)
        );

        SkillGapIdentifier.SkillGapResult gapResult =
                new SkillGapIdentifier.SkillGapResult(List.of("Spring Boot"), List.of("Spring Boot"), 0.6);

        when(profileAnalyzer.analyze(student)).thenReturn(mockAnalysis);
        when(projectMatcher.matchProjects(any(), any(), anyInt())).thenReturn(matchResult);
        when(agentTools.rankProjects(any(), any(), any(), any())).thenReturn(List.of(rankedProject));
        when(skillGapIdentifier.identify(any(), any())).thenReturn(gapResult);
        when(explanationGenerator.generate(any(), any(), any(), anyDouble()))
                .thenReturn("Java skills make you a great fit for this project.");

        RecommendationRequest request = new RecommendationRequest();
        request.setStudentId(1L);
        request.setSessionId("test-session");

        // Act
        List<RecommendationResponse> results = orchestrator.runRecommendationPipeline(student, request);

        // Assert
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getProject().getTitle()).isEqualTo("Finance Tracker API");
        assertThat(results.get(0).getFinalScore()).isEqualTo(0.85);
        assertThat(results.get(0).getSkillGaps()).contains("Spring Boot");

        verify(profileAnalyzer, times(1)).analyze(student);
        verify(projectMatcher, times(1)).matchProjects(any(), any(), anyInt());
        verify(agentTools, times(1)).rankProjects(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Pipeline returns empty list when no candidates found")
    void runPipeline_returnsEmpty_whenNoCandidates() {
        ProjectMatcher.MatchResult emptyResult = new ProjectMatcher.MatchResult(
                List.of(), Map.of(), Map.of());

        when(profileAnalyzer.analyze(student)).thenReturn(mockAnalysis);
        when(projectMatcher.matchProjects(any(), any(), anyInt())).thenReturn(emptyResult);

        RecommendationRequest request = new RecommendationRequest();
        request.setStudentId(1L);
        request.setSessionId("test-session");

        List<RecommendationResponse> results = orchestrator.runRecommendationPipeline(student, request);

        assertThat(results).isEmpty();
        verify(agentTools, never()).rankProjects(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Chat message is handled using LLM with session context")
    void handleChatMessage_returnsAgentResponse() {
        String sessionId = "chat-session-1";
        StateManager.AgentState state = new StateManager.AgentState(sessionId);

        when(stateManager.getOrCreateState(sessionId)).thenReturn(state);
        when(llmService.generateText(anyString(), anyString()))
                .thenReturn("You should build the Finance Tracker API project!");

        String response = orchestrator.handleChatMessage(sessionId, 1L, "What project should I build?");

        assertThat(response).contains("Finance Tracker API");
        verify(llmService, times(1)).generateText(anyString(), anyString());
        verify(stateManager, times(2)).addMessage(eq(sessionId), anyString(), anyString());
    }
}
