package com.projectrecommender.agent.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory vector store for semantic memory.
 * Stores embeddings keyed by arbitrary labels (e.g., student query embeddings).
 */
@Component
@Slf4j
public class VectorMemory {

    /** key -> float array embedding */
    private final Map<String, float[]> embeddings = new ConcurrentHashMap<>();

    /**
     * Store an embedding with a given key.
     */
    public void store(String key, float[] embedding) {
        embeddings.put(key, embedding);
    }

    /**
     * Retrieve an embedding by key.
     */
    public Optional<float[]> retrieve(String key) {
        return Optional.ofNullable(embeddings.get(key));
    }

    /**
     * Find the top-K most similar stored embeddings to a query vector.
     */
    public List<Map.Entry<String, Double>> findSimilar(float[] queryVector, int topK) {
        List<Map.Entry<String, Double>> scores = new ArrayList<>();
        for (Map.Entry<String, float[]> entry : embeddings.entrySet()) {
            double sim = cosineSimilarity(queryVector, entry.getValue());
            scores.add(Map.entry(entry.getKey(), sim));
        }
        scores.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return scores.subList(0, Math.min(topK, scores.size()));
    }

    /**
     * Remove a stored embedding.
     */
    public void remove(String key) {
        embeddings.remove(key);
    }

    /**
     * Clear all stored embeddings.
     */
    public void clear() {
        embeddings.clear();
    }

    public int size() { return embeddings.size(); }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }
}
