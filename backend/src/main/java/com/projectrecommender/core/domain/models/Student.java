package com.projectrecommender.core.domain.models;

import com.projectrecommender.core.domain.enums.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private Long id;
    private String name;
    private ExperienceLevel experienceLevel;
    private String careerGoal;
    private List<String> skills;
    private List<String> interests;
}
