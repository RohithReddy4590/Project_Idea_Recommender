package com.projectrecommender.knowledgebase.service;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.vector.EmbeddingService;
import com.projectrecommender.knowledgebase.vector.VectorIndexService;
import com.projectrecommender.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Manages the project knowledge base.
 * Handles CRUD operations and embedding generation for projects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectKnowledgeService {

    private final ProjectRepository projectRepository;
    private final EmbeddingService embeddingService;
    private final VectorIndexService vectorIndexService;
    private final LLMService llmService;

    /**
     * Save a project and compute its embedding vector.
     */
    @Transactional
    public Project saveProjectWithEmbedding(Project project) {
        String embeddingText = embeddingService.buildProjectEmbeddingText(project);
        List<Float> embedding = llmService.generateEmbedding(embeddingText);

        if (embedding != null && !embedding.isEmpty()) {
            String serialized = embeddingService.serializeEmbedding(embedding);
            project.setEmbeddingVector(serialized);
        } else {
            log.warn("Could not generate embedding for project: {}", project.getTitle());
        }

        Project saved = projectRepository.save(project);

        if (project.getEmbeddingVector() != null) {
            double[] vector = embeddingService.parseEmbedding(project.getEmbeddingVector());
            vectorIndexService.updateIndex(saved.getId(), vector);
        }

        return saved;
    }

    /**
     * Get all projects from the knowledge base.
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Get a project by ID.
     */
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Get projects filtered by domain.
     */
    public List<Project> getProjectsByDomain(String domain) {
        return projectRepository.findByDomain(domain);
    }

    /**
     * Get projects filtered by difficulty.
     */
    public List<Project> getProjectsByDifficulty(Project.Difficulty difficulty) {
        return projectRepository.findByDifficulty(difficulty);
    }

    /**
     * Get all unique domains.
     */
    public List<String> getAllDomains() {
        return projectRepository.findAllDomains();
    }

    /**
     * Recompute embeddings for all projects.
     */
    @Transactional
    public void recomputeAllEmbeddings() {
        log.info("Starting recomputation of all project embeddings...");
        List<Project> projects = projectRepository.findAll();
        int count = 0;
        for (Project project : projects) {
            try {
                String text = embeddingService.buildProjectEmbeddingText(project);
                List<Float> embedding = llmService.generateEmbedding(text);
                if (embedding != null && !embedding.isEmpty()) {
                    project.setEmbeddingVector(embeddingService.serializeEmbedding(embedding));
                    projectRepository.save(project);
                    double[] vector = embeddingService.parseEmbedding(project.getEmbeddingVector());
                    vectorIndexService.updateIndex(project.getId(), vector);
                    count++;
                }
            } catch (Exception e) {
                log.error("Failed to compute embedding for project {}: {}", project.getId(), e.getMessage());
            }
        }
        log.info("Recomputed embeddings for {} projects.", count);
    }
}
