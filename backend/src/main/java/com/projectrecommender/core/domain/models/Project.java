package com.projectrecommender.core.domain.models;

import com.projectrecommender.core.domain.enums.Difficulty;
import com.projectrecommender.core.domain.enums.Domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Domain domain;
    private List<String> techStack;
    private List<String> skillsRequired;
    private List<String> learningOutcomes;
}
