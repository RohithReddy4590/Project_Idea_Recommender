package com.projectrecommender.agent.memory;

import com.projectrecommender.agent.reasoning.ProfileAnalyzer.ProfileAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Long-term memory storage for student profiles.
 * Stores analyzed profiles across sessions for continuity.
 */
@Component
@Slf4j
public class ProfileMemory {

    private final Map<Long, ProfileMemoryEntry> store = new ConcurrentHashMap<>();

    /**
     * Save a student profile analysis to memory.
     */
    public void saveProfile(Long studentId, ProfileAnalysis analysis) {
        store.put(studentId, new ProfileMemoryEntry(analysis, LocalDateTime.now()));
        log.debug("Profile saved to memory for student: {}", studentId);
    }

    /**
     * Retrieve the most recent profile analysis for a student.
     */
    public Optional<ProfileAnalysis> getProfile(Long studentId) {
        ProfileMemoryEntry entry = store.get(studentId);
        return Optional.ofNullable(entry != null ? entry.getAnalysis() : null);
    }

    /**
     * Check if a profile exists in memory.
     */
    public boolean hasProfile(Long studentId) {
        return store.containsKey(studentId);
    }

    /**
     * Remove a student profile from memory.
     */
    public void evictProfile(Long studentId) {
        store.remove(studentId);
    }

    /**
     * Clear all profiles older than the given threshold.
     */
    public void evictOlderThan(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        store.entrySet().removeIf(e -> e.getValue().getSavedAt().isBefore(threshold));
    }

    public int size() { return store.size(); }

    // ── Inner Record ──────────────────────────────────────────────────────────

    private static class ProfileMemoryEntry {
        private final ProfileAnalysis analysis;
        private final LocalDateTime savedAt;

        ProfileMemoryEntry(ProfileAnalysis analysis, LocalDateTime savedAt) {
            this.analysis = analysis;
            this.savedAt = savedAt;
        }

        public ProfileAnalysis getAnalysis() { return analysis; }
        public LocalDateTime getSavedAt() { return savedAt; }
    }
}
