package com.projectrecommender.api.controller;

import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.api.dto.response.SkillGapResponse;
import com.projectrecommender.agent.reasoning.SkillGapIdentifier;
import com.projectrecommender.knowledgebase.entity.Project;
import com.projectrecommender.knowledgebase.entity.RecommendationHistory;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.repository.ProjectRepository;
import com.projectrecommender.knowledgebase.repository.StudentRepository;
import com.projectrecommender.services.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for project recommendations.
 */
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SkillGapIdentifier skillGapIdentifier;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;

    /**
     * POST /recommendations
     * Generate personalized project recommendations for a student.
     */
    @PostMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * GET /recommendations/history/{studentId}
     * Retrieve recommendation history for a student.
     */
    @GetMapping("/history/{studentId}")
    public ResponseEntity<List<RecommendationHistory>> getHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(recommendationService.getHistory(studentId));
    }

    /**
     * POST /recommendations/feedback/{historyId}
     * Submit feedback on a recommendation.
     */
    @PostMapping("/feedback/{historyId}")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @PathVariable Long historyId,
            @RequestBody Map<String, String> body) {
        String feedbackStr = body.get("feedback");
        RecommendationHistory.Feedback feedback = RecommendationHistory.Feedback.valueOf(feedbackStr.toUpperCase());
        recommendationService.submitFeedback(historyId, feedback);
        return ResponseEntity.ok(Map.of("message", "Feedback recorded successfully."));
    }

    /**
     * GET /recommendations/skill-gap/{studentId}/{projectId}
     * Get skill gap analysis between student skills and project requirements.
     */
    @GetMapping("/skill-gap/{studentId}/{projectId}")
    public ResponseEntity<SkillGapResponse> getSkillGap(
            @PathVariable Long studentId,
            @PathVariable Long projectId) {

        Student student = studentRepository.findByIdWithSkills(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Set<String> skillNames = student.getStudentSkills().stream()
                .map(ss -> ss.getSkill().getName())
                .collect(Collectors.toSet());

        SkillGapIdentifier.SkillGapResult result = skillGapIdentifier.identify(project, skillNames);

        SkillGapResponse response = SkillGapResponse.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .missingSkills(result.getMissingSkills())
                .learningPath(result.getLearningPath())
                .readinessScore(result.getReadinessScore())
                .readyNow(result.isReadyNow())
                .build();

        return ResponseEntity.ok(response);
    }
}
