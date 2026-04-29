package com.projectrecommender.knowledgebase.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents domain and difficulty categories for projects.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column
    private String type; // DOMAIN or DIFFICULTY

    @Column(name = "parent_category")
    private String parentCategory;
}
