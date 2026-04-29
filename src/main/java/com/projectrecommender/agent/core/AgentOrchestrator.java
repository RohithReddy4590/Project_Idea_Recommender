package com.projectrecommender.agent.core;

import com.projectrecommender.agent.memory.InteractionHistory;
import com.projectrecommender.agent.memory.ProfileMemory;
import com.projectrecommender.agent.reasoning.*;
import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import com.projectrecommender.agent.reasoning.ProjectMatcher.MatchResult;
import com.projectrecommender.agent.reasoning.SkillGapIdentifier.SkillGapResult;
import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.services.LLMService;
import com.projectrecommender.services.RankingService.RankedProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Main AI Agent Orchestrator.
 *
 * Executes the full recommendation pipeline:
 *   1. Profile Analysis
 *   2. Rule-Based Filtering
 *   3. Semantic Vector Search
 *   4. Project Matching
 *   5. Skill Gap Detection
 *   6. LLM Ranking
 *   7. Explanation Generation
 *   8. Return Final Recommendations
 *
 * This is the central controller that coordinates all reasoning, memory, and tool calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestrator {

    private final ProfileAnalyzer profileAnalyzer;
    private final ProjectMatcher projectMatcher;
    private final SkillGapIdentifier skillGapIdentifier;
    private final ExplanationGenerator explanationGenerator;
    private final AgentTools agentTools;
    private final StateManager stateManager;
    private final ProfileMemory profileMemory;
    private final InteractionHistory interactionHistory;
    private final LLMService llmService;

    @Value("${agent.max-recommendations:10}")
    private int maxRecommendations;

    /**
     * Run the full AI agent recommendation pipeline for a student.
     *
     * @param student  The student requesting recommendations
     * @param request  The recommendation request with optional preferences
     * @return         Ordered list of personalized recommendations
     */
    public List<RecommendationResponse> runRecommendationPipeline(Student student, RecommendationRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId()
                : "session-" + student.getId() + "-" + System.currentTimeMillis();

        log.info("=== Agent Pipeline Start | Student: {} | Session: {} ===", student.getName(), sessionId);

        // ── STEP 1: Profile Analysis ─────────────────────────────────────────
        log.info("[Step 1] Analyzing student profile...");
        ProfileAnalysis analysis = profileAnalyzer.analyze(student);
        stateManager.updateProfile(sessionId, analysis);
        profileMemory.saveProfile(student.getId(), analysis);

        log.info("[Step 1] Skill Level: {}, Domain: {}, Skills: {}",
                analysis.getSkillLevel(), analysis.getDomainFocus(), analysis.getSkillNames());

        // ── STEP 2–4: Filter + Semantic Search + Matching ────────────────────
        log.info("[Step 2-4] Running project matching (rules + semantic search)...");
        int candidatePoolSize = maxRecommendations * 4;
        MatchResult matchResult = projectMatcher.matchProjects(analysis, student, candidatePoolSize);

        List<Project> candidates = matchResult.getProjects();
        log.info("[Step 2-4] {} candidate projects found.", candidates.size());

        if (candidates.isEmpty()) {
            log.warn("[Agent] No candidate projects found. Returning empty list.");
            return List.of();
        }

        // ── STEP 5: Skill Gap Detection for each candidate ───────────────────
        log.info("[Step 5] Detecting skill gaps for each candidate...");

        // ── STEP 6: LLM Ranking ───────────────────────────────────────────────
        log.info("[Step 6] Running hybrid LLM ranking...");
        List<RankedProject> ranked = agentTools.rankProjects(
                candidates,
                matchResult.getSkillScores(),
                matchResult.getSemanticScores(),
                analysis.getProfileSummary()
        );

        // Take top N
        List<RankedProject> topRanked = ranked.stream()
                .limit(maxRecommendations)
                .toList();

        // ── STEP 7: Explanation Generation ───────────────────────────────────
        log.info("[Step 7] Generating explanations for top {} recommendations...", topRanked.size());
        List<RecommendationResponse> responses = new ArrayList<>();

        for (RankedProject rankedProject : topRanked) {
            Project project = rankedProject.getProject();
            SkillGapResult gapResult = skillGapIdentifier.identify(project, analysis.getSkillNames());

            String explanation;
            try {
                explanation = explanationGenerator.generate(project, analysis, gapResult, rankedProject.getFinalScore());
            } catch (Exception e) {
                log.warn("LLM explanation failed for project {}, using fallback.", project.getTitle());
                explanation = explanationGenerator.generateFallback(project, analysis, gapResult);
            }

            RecommendationResponse response = RecommendationResponse.builder()
                    .project(project)
                    .finalScore(rankedProject.getFinalScore())
                    .skillMatchScore(rankedProject.getSkillScore())
                    .semanticScore(rankedProject.getSemanticScore())
                    .llmScore(rankedProject.getLlmScore())
                    .explanation(explanation)
                    .skillGaps(gapResult.getMissingSkills())
                    .learningPath(gapResult.getLearningPath())
                    .readinessScore(gapResult.getReadinessScore())
                    .sessionId(sessionId)
                    .build();

            responses.add(response);
        }

        // ── STEP 8: Record in state and interaction history ───────────────────
        stateManager.recordRecommendations(sessionId, responses);
        interactionHistory.record(student.getId(), sessionId, responses.size());
        stateManager.addMessage(sessionId, "assistant",
                String.format("Generated %d recommendations for %s.", responses.size(), student.getName()));

        log.info("=== Agent Pipeline Complete | {} recommendations returned ===", responses.size());
        return responses;
    }

    /**
     * Handle a conversational chat message within an existing agent session.
     * Uses the student's profile context and previous recommendations.
     *
     * @param sessionId      Active session ID
     * @param studentId      Student ID
     * @param userMessage    User's chat message
     * @return               Agent response
     */
    public String handleChatMessage(String sessionId, Long studentId, String userMessage) {
        log.info("[Agent Chat] Session: {}, Message: {}", sessionId, userMessage);

        StateManager.AgentState state = stateManager.getOrCreateState(sessionId);
        stateManager.addMessage(sessionId, "user", userMessage);

        // Build a context-aware system prompt
        String systemPrompt = buildChatSystemPrompt(state);

        // Build conversation context
        StringBuilder context = new StringBuilder();
        if (state.getProfileAnalysis() != null) {
            context.append("Student Profile: ").append(state.getProfileAnalysis().getProfileSummary()).append("\n\n");
        }
        if (!state.getPreviousRecommendations().isEmpty()) {
            context.append("Previously recommended projects:\n");
            state.getPreviousRecommendations().stream().limit(5).forEach(r ->
                    context.append("- ").append(r.getProject().getTitle()).append("\n"));
            context.append("\n");
        }
        context.append("Student question: ").append(userMessage);

        String response;
        try {
            response = llmService.generateText(systemPrompt, context.toString());
        } catch (Exception e) {
            log.warn("LLM chat failed, using fallback.");
            response = "I'm currently unable to connect to the AI service. " +
                       "But you can still view your recommended projects and their skill gap analysis!";
        }
        stateManager.addMessage(sessionId, "assistant", response);

        return response;
    }

    /**
     * Build a dynamic system prompt for chat using session context.
     */
    private String buildChatSystemPrompt(StateManager.AgentState state) {
        String base;
        try {
            var stream = getClass().getClassLoader().getResourceAsStream("prompts/agent_system_prompt.txt");
            base = stream != null ? new String(stream.readAllBytes()) : getDefaultSystemPrompt();
        } catch (Exception e) {
            base = getDefaultSystemPrompt();
        }
        return base + "\n\nYou have context about the student's profile and prior recommendations. " +
               "Answer questions helpfully and specifically. Interaction #" + state.getInteractionCount();
    }

    private String getDefaultSystemPrompt() {
        return "You are an AI career advisor helping students choose portfolio projects. " +
               "Be encouraging, specific, and actionable in your responses.";
    }
}
