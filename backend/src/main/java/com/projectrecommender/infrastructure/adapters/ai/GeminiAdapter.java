package com.projectrecommender.infrastructure.adapters.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.core.domain.models.LlmAnalysisResponse;
import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import com.projectrecommender.core.ports.outbound.OpenAIServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GeminiAdapter implements OpenAIServicePort {

    @org.springframework.beans.factory.annotation.Value("${gemini.api.key}")
    private String geminiApiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    @Override
    public Map<Long, String> getExplanations(Student student, List<Project> projects) {
        return getDetailedAnalysis(student, projects).getExplanations();
    }

    @Override
    public Map<Long, Double> getLlmScores(Student student, List<Project> projects) {
        return getDetailedAnalysis(student, projects).getScores();
    }

    @Override
    public Map<Long, List<String>> getSkillGaps(Student student, List<Project> projects) {
        return getDetailedAnalysis(student, projects).getSkillGaps();
    }

    @Override
    public LlmAnalysisResponse getDetailedAnalysis(Student student, List<Project> projects) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return createFallback(projects);
        }

        try {
            String prompt = buildPrompt(student, projects);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt + "\nIMPORTANT: Return ONLY raw JSON. No markdown blocks.")))
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(API_URL_BASE + geminiApiKey, entity, String.class);
            System.out.println("DEBUG: [Gemini] Raw Response: " + response);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            System.out.println("DEBUG: [Gemini] Extracted Content: " + content);
            
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode resultNode = objectMapper.readTree(content);

            LlmAnalysisResponse result = new LlmAnalysisResponse();
            for (Project p : projects) {
                JsonNode pNode = resultNode.path(String.valueOf(p.getId()));
                result.getScores().put(p.getId(), pNode.path("score").asDouble(5.0));
                result.getExplanations().put(p.getId(), pNode.path("explanation").asText("Recommended based on profile."));
                List<String> gaps = new ArrayList<>();
                pNode.path("skill_gap").forEach(g -> gaps.add(g.asText()));
                result.getSkillGaps().put(p.getId(), gaps);
            }
            return result;

        } catch (Exception e) {
            System.out.println("DEBUG: [Gemini] Error: " + e.getMessage());
            return createFallback(projects);
        }
    }

    private LlmAnalysisResponse createFallback(List<Project> projects) {
        LlmAnalysisResponse fallback = new LlmAnalysisResponse();
        projects.forEach(p -> {
            fallback.getScores().put(p.getId(), 5.0);
            fallback.getExplanations().put(p.getId(), "Recommended based on profile.");
            fallback.getSkillGaps().put(p.getId(), List.of());
        });
        return fallback;
    }

    private String buildPrompt(Student student, List<Project> projects) {
        StringBuilder sb = new StringBuilder();
        sb.append("As an elite technical career coach, analyze how these projects help this student reach their specific goal.\n");
        sb.append("For each project, provide a JSON object where keys are project IDs.\n");
        sb.append("Each value must be an object with 'score' (0-10), 'explanation' (3-4 sentences), and 'skill_gap' (list of strings).\n\n");
        sb.append("Student Profile:\n");
        sb.append("- Skills: ").append(String.join(", ", student.getSkills())).append("\n");
        sb.append("- Goal: ").append(student.getCareerGoal()).append("\n");
        sb.append("- Experience: ").append(student.getExperienceLevel()).append("\n\n");
        sb.append("Projects to evaluate:\n");
        for (Project p : projects) {
            sb.append("ID: ").append(p.getId()).append("\n");
            sb.append("Title: ").append(p.getTitle()).append("\n");
            sb.append("Description: ").append(p.getDescription()).append("\n\n");
        }
        return sb.toString();
    }

    @Override
    public List<Project> generateDynamicProjects(Student student, int count) {
        try {
            String prompt = String.format(
                "Generate %d unique and innovative project ideas based on the student profile. " +
                "Return a JSON object with a 'projects' array containing: title, description, difficulty, domain, techStack, skillsRequired, learningOutcomes. " +
                "Profile: Skills: %s, Goal: %s, Experience: %s",
                count, String.join(", ", student.getSkills()), student.getCareerGoal(), student.getExperienceLevel()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt + "\nReturn ONLY raw JSON.")))));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(API_URL_BASE + geminiApiKey, entity, String.class);
            System.out.println("DEBUG: [Gemini-Gen] Raw Response: " + response);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Clean up potential markdown blocks
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            System.out.println("DEBUG: [Gemini] Raw content: " + content);
            
            JsonNode resultNode = objectMapper.readTree(content);
            JsonNode projectsNode = resultNode.isObject() && resultNode.has("projects") ? resultNode.get("projects") : resultNode;
            
            List<Project> projects = new ArrayList<>();
            if (projectsNode.isArray()) {
                for (JsonNode pNode : projectsNode) {
                    projects.add(parseProject(pNode));
                }
            }
            System.out.println("DEBUG: [Gemini] Successfully generated " + projects.size() + " dynamic projects.");
            return projects;

        } catch (Exception e) {
            System.out.println("DEBUG: [Gemini] Generation Error: " + e.getMessage());
            return List.of();
        }
    }

    private Project parseProject(JsonNode pNode) {
        return Project.builder()
                .id(new Random().nextLong(9000, 10000))
                .title(pNode.path("title").asText())
                .description(pNode.path("description").asText())
                .difficulty(parseDifficulty(pNode.path("difficulty").asText("INTERMEDIATE")))
                .domain(parseDomain(pNode.path("domain").asText("FULLSTACK")))
                .techStack(parseList(pNode.path("techStack")))
                .skillsRequired(parseList(pNode.path("skillsRequired")))
                .learningOutcomes(parseList(pNode.path("learningOutcomes")))
                .build();
    }

    private com.projectrecommender.core.domain.enums.Difficulty parseDifficulty(String val) {
        String s = val.toUpperCase();
        if (s.contains("BEGINNER")) return com.projectrecommender.core.domain.enums.Difficulty.BEGINNER;
        if (s.contains("ADVANCED")) return com.projectrecommender.core.domain.enums.Difficulty.ADVANCED;
        return com.projectrecommender.core.domain.enums.Difficulty.INTERMEDIATE;
    }

    private com.projectrecommender.core.domain.enums.Domain parseDomain(String val) {
        String s = val.toUpperCase();
        for (com.projectrecommender.core.domain.enums.Domain d : com.projectrecommender.core.domain.enums.Domain.values()) {
            if (s.contains(d.name())) return d;
        }
        return com.projectrecommender.core.domain.enums.Domain.FULLSTACK;
    }

    private List<String> parseList(JsonNode node) {
        List<String> list = new ArrayList<>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }
}
