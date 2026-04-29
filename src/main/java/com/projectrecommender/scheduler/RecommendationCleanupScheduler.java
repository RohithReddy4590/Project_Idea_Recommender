package com.projectrecommender.scheduler;

import com.projectrecommender.agent.core.StateManager;
import com.projectrecommender.agent.memory.ProfileMemory;
import com.projectrecommender.knowledgebase.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task that cleans up stale recommendation history and session data.
 * Runs every Sunday at 04:00 AM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationCleanupScheduler {

    private final RecommendationHistoryRepository historyRepository;
    private final StateManager stateManager;
    private final ProfileMemory profileMemory;

    @Value("${agent.memory-ttl-days:30}")
    private int memoryTtlDays;

    /**
     * Weekly cleanup of old recommendation history records.
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    @Transactional
    public void cleanupOldHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(memoryTtlDays);
        log.info("[Scheduler] Cleaning recommendation history older than {}", cutoff);
        historyRepository.deleteOlderThan(cutoff);
        log.info("[Scheduler] Recommendation history cleanup complete.");
    }

    /**
     * Every hour — remove stale in-memory sessions (older than 4 hours).
     */
    @Scheduled(fixedDelay = 3_600_000)
    public void cleanupStaleSessions() {
        stateManager.cleanupStaleSessions(240); // 4 hours
        log.debug("[Scheduler] Stale sessions cleaned up.");
    }

    /**
     * Daily — evict old profiles from memory cache.
     */
    @Scheduled(cron = "0 30 3 * * *")
    public void evictOldProfileMemory() {
        profileMemory.evictOlderThan(memoryTtlDays);
        log.info("[Scheduler] Old profile memory evicted. Current size: {}", profileMemory.size());
    }
}
