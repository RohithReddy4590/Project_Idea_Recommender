package com.projectrecommender.agent.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks interaction history per student and session.
 * Used by the agent to provide continuity across multiple requests.
 */
@Component
@Slf4j
public class InteractionHistory {

    /** studentId -> list of interaction records */
    private final Map<Long, List<InteractionRecord>> history = new ConcurrentHashMap<>();

    /**
     * Record an interaction.
     *
     * @param studentId             Student ID
     * @param sessionId             Session ID
     * @param recommendationCount   Number of recommendations returned
     */
    public void record(Long studentId, String sessionId, int recommendationCount) {
        history.computeIfAbsent(studentId, k -> new ArrayList<>())
                .add(new InteractionRecord(sessionId, recommendationCount, LocalDateTime.now()));
        log.debug("Interaction recorded: studentId={}, session={}", studentId, sessionId);
    }

    /**
     * Get all interactions for a student.
     */
    public List<InteractionRecord> getHistory(Long studentId) {
        return history.getOrDefault(studentId, Collections.emptyList());
    }

    /**
     * Get the number of times the student has interacted with the agent.
     */
    public int getInteractionCount(Long studentId) {
        return history.getOrDefault(studentId, Collections.emptyList()).size();
    }

    /**
     * Check if student is a returning user.
     */
    public boolean isReturningUser(Long studentId) {
        return getInteractionCount(studentId) > 1;
    }

    /**
     * Clear history for a student.
     */
    public void clearHistory(Long studentId) {
        history.remove(studentId);
    }

    // ── Inner Record ──────────────────────────────────────────────────────────

    public static class InteractionRecord {
        private final String sessionId;
        private final int recommendationCount;
        private final LocalDateTime timestamp;

        public InteractionRecord(String sessionId, int recommendationCount, LocalDateTime timestamp) {
            this.sessionId = sessionId;
            this.recommendationCount = recommendationCount;
            this.timestamp = timestamp;
        }

        public String getSessionId() { return sessionId; }
        public int getRecommendationCount() { return recommendationCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
