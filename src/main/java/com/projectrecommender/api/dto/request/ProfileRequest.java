package com.projectrecommender.api.dto.request;

import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.entity.StudentSkill;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating or updating a student profile.
 */
@Data
public class ProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String careerGoal;

    private String domainInterests;

    private Student.ExperienceLevel experienceLevel;

    private List<SkillEntry> skills;

    @Data
    public static class SkillEntry {
        private String skillName;
        private StudentSkill.ProficiencyLevel proficiencyLevel;
        private Double yearsExperience;
    }
}
