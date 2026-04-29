package com.projectrecommender.knowledgebase.vector;

import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.services.LLMService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages in-memory vector index for fast semantic similarity search.
 * Implements cosine similarity between project and query embeddings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorIndexService {

    private final ProjectRepository projectRepository;
    private final EmbeddingService embeddingService;
    private final LLMService llmService;

    /** In-memory index: projectId -> embedding vector */
    private final Map<Long, double[]> vectorIndex = new ConcurrentHashMap<>();

    /**
     * Load all project embeddings into memory on startup.
     */
    @PostConstruct
    public void loadIndex() {
        log.info("Loading vector index from database...");
        List<Project> projects = projectRepository.findAllWithEmbeddings();
        for (Project project : projects) {
            if (project.getEmbeddingVector() != null && !project.getEmbeddingVector().isBlank()) {
                double[] vector = embeddingService.parseEmbedding(project.getEmbeddingVector());
                if (vector.length > 0) {
                    vectorIndex.put(project.getId(), vector);
                }
            }
        }
        log.info("Vector index loaded with {} projects.", vectorIndex.size());
    }

    /**
     * Add or update a project's embedding in the index.
     */
    public void updateIndex(Long projectId, double[] embedding) {
        vectorIndex.put(projectId, embedding);
    }

    /**
     * Search for top-K most similar projects given a query text.
     *
     * @param queryText  The student profile query text
     * @param topK       Number of top results to return
     * @return           Sorted list of (projectId, similarityScore) pairs
     */
    public List<Map.Entry<Long, Double>> searchSimilar(String queryText, int topK) {
        if (vectorIndex.isEmpty()) {
            log.warn("Vector index is empty. Skipping semantic search.");
            return Collections.emptyList();
        }

        List<Float> queryEmbedding = llmService.generateEmbedding(queryText);
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            log.warn("Failed to generate query embedding.");
            return Collections.emptyList();
        }

        double[] queryVector = queryEmbedding.stream().mapToDouble(Float::doubleValue).toArray();

        Map<Long, Double> scores = new HashMap<>();
        for (Map.Entry<Long, double[]> entry : vectorIndex.entrySet()) {
            double similarity = cosineSimilarity(queryVector, entry.getValue());
            scores.put(entry.getKey(), similarity);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Compute cosine similarity between two vectors.
     */
    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length || a.length == 0) return 0.0;
        RealVector va = new ArrayRealVector(a);
        RealVector vb = new ArrayRealVector(b);
        double dot = va.dotProduct(vb);
        double norm = va.getNorm() * vb.getNorm();
        return norm == 0 ? 0.0 : dot / norm;
    }

    /**
     * Return current index size.
     */
    public int getIndexSize() {
        return vectorIndex.size();
    }

    /**
     * Clear and reload index.
     */
    public void reloadIndex() {
        vectorIndex.clear();
        loadIndex();
    }
}
