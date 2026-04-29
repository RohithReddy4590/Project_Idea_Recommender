package com.projectrecommender.knowledgebase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"student"})
public class RecommendationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "recommendation_score")
    private Double recommendationScore;

    @Column(name = "skill_match_score")
    private Double skillMatchScore;

    @Column(name = "semantic_score")
    private Double semanticScore;

    @Column(name = "llm_score")
    private Double llmScore;

    @Column(name = "explanation", length = 2000)
    private String explanation;

    @Column(name = "skill_gaps", length = 1000)
    private String skillGaps;

    @Column(name = "feedback")
    @Enumerated(EnumType.STRING)
    private Feedback feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "session_id")
    private String sessionId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Feedback {
        LIKED, DISLIKED, NEUTRAL, NOT_REVIEWED
    }
}