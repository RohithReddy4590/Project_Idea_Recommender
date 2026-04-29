package com.projectrecommender.services;

import com.projectrecommender.agent.core.AgentOrchestrator;
import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.RecommendationResponse;
import com.projectrecommender.knowledgebase.entity.RecommendationHistory;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.repository.RecommendationHistoryRepository;
import com.projectrecommender.knowledgebase.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Main recommendation service.
 * Delegates to the agent orchestrator and persists results.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final AgentOrchestrator agentOrchestrator;
    private final StudentRepository studentRepository;
    private final RecommendationHistoryRepository historyRepository;

    /**
     * Generate recommendations for a student using the full agent pipeline.
     */
    @Transactional
    public List<RecommendationResponse> getRecommendations(RecommendationRequest request) {
        log.info("Generating recommendations for student ID: {}", request.getStudentId());

        Student student = studentRepository.findByIdWithSkills(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + request.getStudentId()));

        List<RecommendationResponse> responses = agentOrchestrator.runRecommendationPipeline(student, request);

        // Persist to recommendation history
        persistRecommendations(student, responses, request.getSessionId());

        return responses;
    }

    /**
     * Submit feedback on a recommendation.
     */
    @Transactional
    public void submitFeedback(Long historyId, RecommendationHistory.Feedback feedback) {
        RecommendationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation history not found: " + historyId));
        history.setFeedback(feedback);
        historyRepository.save(history);
        log.info("Feedback '{}' saved for recommendation history ID: {}", feedback, historyId);
    }

    /**
     * Get recommendation history for a student.
     */
    public List<RecommendationHistory> getHistory(Long studentId) {
        return historyRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    private void persistRecommendations(Student student, List<RecommendationResponse> responses, String sessionId) {
        for (RecommendationResponse response : responses) {
            try {
                RecommendationHistory history = RecommendationHistory.builder()
                        .student(student)
                        .project(response.getProject())
                        .recommendationScore(response.getFinalScore())
                        .skillMatchScore(response.getSkillMatchScore())
                        .semanticScore(response.getSemanticScore())
                        .llmScore(response.getLlmScore())
                        .explanation(response.getExplanation())
                        .skillGaps(String.join(", ", response.getSkillGaps()))
                        .feedback(RecommendationHistory.Feedback.NOT_REVIEWED)
                        .sessionId(sessionId)
                        .build();
                historyRepository.save(history);
            } catch (Exception e) {
                log.error("Failed to persist recommendation history: {}", e.getMessage());
            }
        }
    }
}
