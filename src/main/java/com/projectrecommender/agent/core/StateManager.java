package com.projectrecommender.agent.core;

import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages in-session agent state per session ID.
 * Tracks student profile state, interaction history, and previous recommendations.
 */
@Component
@Slf4j
public class StateManager {

    /** Session-scoped state map: sessionId -> AgentState */
    private final Map<String, AgentState> sessionStates = new ConcurrentHashMap<>();

    /**
     * Initialize or retrieve agent state for a session.
     */
    public AgentState getOrCreateState(String sessionId) {
        return sessionStates.computeIfAbsent(sessionId, id -> {
            log.debug("Creating new agent state for session: {}", id);
            return new AgentState(id);
        });
    }

    /**
     * Update the profile analysis for a session.
     */
    public void updateProfile(String sessionId, ProfileAnalysis analysis) {
        AgentState state = getOrCreateState(sessionId);
        state.setProfileAnalysis(analysis);
        state.setLastUpdated(LocalDateTime.now());
    }

    /**
     * Record recommendations for a session.
     */
    public void recordRecommendations(String sessionId, List<RecommendationResponse> recommendations) {
        AgentState state = getOrCreateState(sessionId);
        state.getPreviousRecommendations().clear();
        state.getPreviousRecommendations().addAll(recommendations);
        state.incrementInteractionCount();
        state.setLastUpdated(LocalDateTime.now());
    }

    /**
     * Add a chat message to interaction history.
     */
    public void addMessage(String sessionId, String role, String message) {
        AgentState state = getOrCreateState(sessionId);
        state.getMessages().add(Map.of("role", role, "content", message, "timestamp", LocalDateTime.now().toString()));
    }

    /**
     * Get full conversation history for a session.
     */
    public List<Map<String, String>> getMessages(String sessionId) {
        return sessionStates.containsKey(sessionId)
                ? sessionStates.get(sessionId).getMessages()
                : Collections.emptyList();
    }

    /**
     * Clear a session's state.
     */
    public void clearSession(String sessionId) {
        sessionStates.remove(sessionId);
        log.debug("Cleared agent state for session: {}", sessionId);
    }

    /**
     * Clean up stale sessions older than the given threshold.
     */
    public void cleanupStaleSessions(int maxAgeMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(maxAgeMinutes);
        sessionStates.entrySet().removeIf(entry -> {
            AgentState state = entry.getValue();
            return state.getLastUpdated() != null && state.getLastUpdated().isBefore(threshold);
        });
    }

    // ========================= Inner State Class =========================

    public static class AgentState {
        private final String sessionId;
        private ProfileAnalysis profileAnalysis;
        private final List<RecommendationResponse> previousRecommendations = new ArrayList<>();
        private final List<Map<String, String>> messages = new ArrayList<>();
        private int interactionCount = 0;
        private LocalDateTime lastUpdated;
        private LocalDateTime createdAt;

        public AgentState(String sessionId) {
            this.sessionId = sessionId;
            this.createdAt = LocalDateTime.now();
            this.lastUpdated = LocalDateTime.now();
        }

        public String getSessionId() { return sessionId; }
        public ProfileAnalysis getProfileAnalysis() { return profileAnalysis; }
        public void setProfileAnalysis(ProfileAnalysis profileAnalysis) { this.profileAnalysis = profileAnalysis; }
        public List<RecommendationResponse> getPreviousRecommendations() { return previousRecommendations; }
        public List<Map<String, String>> getMessages() { return messages; }
        public int getInteractionCount() { return interactionCount; }
        public void incrementInteractionCount() { this.interactionCount++; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
