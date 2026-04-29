package com.projectrecommender.scheduler;

import com.projectrecommender.services.EmbeddingFacadeService;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that regenerates missing embeddings and reloads the vector index.
 * Runs every day at 2 AM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingUpdateScheduler {

    private final EmbeddingFacadeService embeddingFacadeService;
    private final VectorIndexService vectorIndexService;

    /**
     * Every day at 02:00 AM — generate embeddings for any project that is missing one.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void updateMissingEmbeddings() {
        log.info("[Scheduler] Running embedding update job...");
        int count = embeddingFacadeService.generateMissingEmbeddings();
        log.info("[Scheduler] Embedding update complete. Generated {} new embeddings.", count);
    }

    /**
     * Every day at 03:00 AM — reload the vector index from the database.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void reloadVectorIndex() {
        log.info("[Scheduler] Reloading vector index...");
        vectorIndexService.reloadIndex();
        log.info("[Scheduler] Vector index reloaded. Size: {}", vectorIndexService.getIndexSize());
    }
}
