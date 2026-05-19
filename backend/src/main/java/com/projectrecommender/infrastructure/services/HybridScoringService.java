package com.projectrecommender.infrastructure.services;

import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class HybridScoringService {

    public double calculateSkillMatchScore(Project project, Student student) {
        if (project.getSkillsRequired() == null || project.getSkillsRequired().isEmpty()) return 1.0;
        
        Set<String> studentSkills = new HashSet<>(student.getSkills());
        long matched = project.getSkillsRequired().stream()
                .filter(s -> studentSkills.contains(s))
                .count();
        
        return (double) matched / project.getSkillsRequired().size();
    }

    public double calculateFinalScore(double skillMatch, double semanticScore, double llmScore) {
        // Normalizing LLM score if it's 0-10
        double normalizedLlm = llmScore / 10.0;
        return (0.4 * skillMatch) + (0.3 * semanticScore) + (0.3 * normalizedLlm);
    }
}
