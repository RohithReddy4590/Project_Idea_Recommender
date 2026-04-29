package com.projectrecommender.agent.reasoning;

import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.entity.StudentSkill;
import com.projectrecommender.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes a student profile to extract structured insights.
 * Part of the agent's reasoning layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileAnalyzer {

    private final LLMService llmService;

    /**
     * Analyze student profile and return a structured summary.
     *
     * @param student  Student entity with skills
     * @return         ProfileAnalysis containing structured insights
     */
    public ProfileAnalysis analyze(Student student) {
        log.debug("Analyzing profile for student: {}", student.getName());

        Set<String> skillNames = extractSkillNames(student);
        String skillLevel = determineSkillLevel(student, skillNames);
        String domainFocus = inferDomainFocus(student, skillNames);
        String profileSummary = buildProfileSummary(student, skillNames);

        // Use LLM to extract deeper career direction insights
        String llmInsight = extractCareerInsights(student, skillNames);

        return new ProfileAnalysis(
                student.getId(),
                student.getName(),
                skillNames,
                skillLevel,
                domainFocus,
                profileSummary,
                llmInsight,
                parseDomainInterests(student.getDomainInterests())
        );
    }

    /**
     * Extract all skill names the student possesses.
     */
    private Set<String> extractSkillNames(Student student) {
        if (student.getStudentSkills() == null) return new HashSet<>();
        return student.getStudentSkills().stream()
                .map(ss -> ss.getSkill().getName())
                .collect(Collectors.toSet());
    }

    /**
     * Determine an overall skill level based on experience.
     */
    private String determineSkillLevel(Student student, Set<String> skills) {
        if (student.getExperienceLevel() != null) {
            return student.getExperienceLevel().name();
        }
        // Infer from number of skills
        int count = skills.size();
        if (count <= 3) return "BEGINNER";
        if (count <= 8) return "INTERMEDIATE";
        return "ADVANCED";
    }

    /**
     * Infer primary domain focus from skills.
     */
    private String inferDomainFocus(Student student, Set<String> skills) {
        if (student.getDomainInterests() != null && !student.getDomainInterests().isBlank()) {
            return student.getDomainInterests().split(",")[0].trim();
        }

        // Heuristic domain detection from skill names
        Set<String> lowerSkills = skills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (lowerSkills.stream().anyMatch(s -> s.contains("machine learning") || s.contains("ai") || s.contains("tensorflow")))
            return "AI/ML";
        if (lowerSkills.stream().anyMatch(s -> s.contains("react") || s.contains("angular") || s.contains("vue")))
            return "Frontend";
        if (lowerSkills.stream().anyMatch(s -> s.contains("spring") || s.contains("django") || s.contains("node")))
            return "Backend";
        if (lowerSkills.stream().anyMatch(s -> s.contains("docker") || s.contains("kubernetes") || s.contains("aws")))
            return "DevOps";
        return "General Software Engineering";
    }

    /**
     * Build a plain-text profile summary for LLM consumption.
     */
    public String buildProfileSummary(Student student, Set<String> skillNames) {
        return String.format(
            "Name: %s | Skills: %s | Interests: %s | Career Goal: %s | Level: %s",
            student.getName(),
            String.join(", ", skillNames),
            student.getDomainInterests() != null ? student.getDomainInterests() : "Not specified",
            student.getCareerGoal() != null ? student.getCareerGoal() : "Not specified",
            student.getExperienceLevel() != null ? student.getExperienceLevel().name() : "Unknown"
        );
    }

    /**
     * Use LLM to extract career direction insights from the profile.
     */
    private String extractCareerInsights(Student student, Set<String> skills) {
        String prompt = String.format(
            "Analyze this student profile and provide 2-3 key career direction insights in one short paragraph:\n" +
            "Skills: %s\nGoal: %s\nInterests: %s",
            String.join(", ", skills),
            student.getCareerGoal() != null ? student.getCareerGoal() : "Not specified",
            student.getDomainInterests() != null ? student.getDomainInterests() : "Not specified"
        );
        try {
            return llmService.generateText(
                    "You are a career advisor analyzing student profiles. Be concise.", prompt);
        } catch (Exception e) {
            log.warn("LLM career insight failed, using fallback.");
            return "Focus on building core competencies in your areas of interest and completing structured portfolio projects.";
        }
    }

    /**
     * Parse comma-separated domain interests into a list.
     */
    private List<String> parseDomainInterests(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        return Arrays.stream(raw.split(",")).map(String::trim).collect(Collectors.toList());
    }

    /**
     * Immutable value object holding the results of profile analysis.
     */
    public static class ProfileAnalysis {
        private final Long studentId;
        private final String name;
        private final Set<String> skillNames;
        private final String skillLevel;
        private final String domainFocus;
        private final String profileSummary;
        private final String llmCareerInsight;
        private final List<String> domainInterests;

        public ProfileAnalysis(Long studentId, String name, Set<String> skillNames,
                                String skillLevel, String domainFocus, String profileSummary,
                                String llmCareerInsight, List<String> domainInterests) {
            this.studentId = studentId;
            this.name = name;
            this.skillNames = skillNames;
            this.skillLevel = skillLevel;
            this.domainFocus = domainFocus;
            this.profileSummary = profileSummary;
            this.llmCareerInsight = llmCareerInsight;
            this.domainInterests = domainInterests;
        }

        public Long getStudentId() { return studentId; }
        public String getName() { return name; }
        public Set<String> getSkillNames() { return skillNames; }
        public String getSkillLevel() { return skillLevel; }
        public String getDomainFocus() { return domainFocus; }
        public String getProfileSummary() { return profileSummary; }
        public String getLlmCareerInsight() { return llmCareerInsight; }
        public List<String> getDomainInterests() { return domainInterests; }
    }
}
