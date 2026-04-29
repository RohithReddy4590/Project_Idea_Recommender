package com.projectrecommender.knowledgebase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a portfolio project in the knowledge base.
 * Each project has skills, domain, difficulty, and an embedding for semantic search.
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "tech_stack", length = 1000)
    private String techStack;

    @Column
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(name = "skills_required", length = 1000)
    private String skillsRequired;

    @Column
    private String domain;

    @Column(name = "learning_outcomes", length = 2000)
    private String learningOutcomes;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "portfolio_impact_score")
    private Double portfolioImpactScore;

    @Column(name = "embedding_vector", columnDefinition = "LONGTEXT")
    @JsonIgnore
    private String embeddingVector;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
