package com.projectrecommender.services;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.vector.EmbeddingService;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service-level facade for embedding operations.
 * Coordinates embedding generation and index updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingFacadeService {

    private final ProjectRepository projectRepository;
    private final EmbeddingService embeddingService;
    private final VectorIndexService vectorIndexService;
    private final LLMService llmService;

    /**
     * Generate and store embedding for a specific project.
     */
    @Transactional
    public boolean generateEmbeddingForProject(Long projectId) {
        return projectRepository.findById(projectId).map(project -> {
            String text = embeddingService.buildProjectEmbeddingText(project);
            List<Float> embedding = llmService.generateEmbedding(text);
            if (embedding != null && !embedding.isEmpty()) {
                project.setEmbeddingVector(embeddingService.serializeEmbedding(embedding));
                projectRepository.save(project);
                vectorIndexService.updateIndex(projectId, embeddingService.parseEmbedding(project.getEmbeddingVector()));
                return true;
            }
            return false;
        }).orElse(false);
    }

    /**
     * Generate embeddings for all projects that don't have one.
     */
    @Transactional
    public int generateMissingEmbeddings() {
        List<Project> all = projectRepository.findAll();
        int count = 0;
        for (Project p : all) {
            if (p.getEmbeddingVector() == null || p.getEmbeddingVector().isBlank()) {
                if (generateEmbeddingForProject(p.getId())) count++;
            }
        }
        vectorIndexService.reloadIndex();
        return count;
    }
}
