package com.projectrecommender.knowledgebase.vector;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Stores precomputed vector embeddings for projects.
 * Used for fast semantic similarity search.
 */
@Entity
@Table(name = "project_embeddings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, unique = true)
    private Long projectId;

    @Column(name = "embedding_text", length = 2000)
    private String embeddingText;

    @Column(name = "embedding_vector", columnDefinition = "TEXT", nullable = false)
    private String embeddingVector; // JSON array of floats

    @Column(name = "embedding_model")
    private String embeddingModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
