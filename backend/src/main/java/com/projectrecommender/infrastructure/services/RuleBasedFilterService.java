package com.projectrecommender.infrastructure.services;

import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleBasedFilterService {

    public List<Project> filter(List<Project> projects, Student student) {
        // Filter projects based on experience level and domain overlap
        // Simple heuristic: if student is beginner, show beginner/intermediate. 
        // If student is senior, show intermediate/advanced.
        return projects.stream()
                .filter(p -> isDifficultyCompatible(p, student))
                .filter(p -> isDomainRelevant(p, student))
                .collect(Collectors.toList());
    }

    private boolean isDomainRelevant(Project project, Student student) {
        String goal = student.getCareerGoal().toUpperCase();
        String domain = project.getDomain().name().toUpperCase();
        
        // If domain is FULLSTACK, it's generally relevant
        if (domain.equals("FULLSTACK")) return true;
        
        // Check if goal contains domain keyword (e.g. "Data Scientist" contains "DATA")
        return goal.contains(domain) || 
               student.getInterests().stream().anyMatch(i -> i.toUpperCase().contains(domain));
    }

    private boolean isDifficultyCompatible(Project project, Student student) {
        switch (student.getExperienceLevel()) {
            case BEGINNER:
                return project.getDifficulty().name().equals("BEGINNER") || project.getDifficulty().name().equals("INTERMEDIATE");
            case INTERMEDIATE:
                return true;
            case SENIOR:
                return project.getDifficulty().name().equals("INTERMEDIATE") || project.getDifficulty().name().equals("ADVANCED");
            default:
                return true;
        }
    }
}
