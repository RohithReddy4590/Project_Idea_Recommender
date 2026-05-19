package com.projectrecommender.core.ports.outbound;

import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import java.util.List;
import java.util.Map;

public interface OpenAIServicePort {
    Map<Long, String> getExplanations(Student student, List<Project> projects);
    Map<Long, Double> getLlmScores(Student student, List<Project> projects);
    Map<Long, List<String>> getSkillGaps(Student student, List<Project> projects);
    com.projectrecommender.core.domain.models.LlmAnalysisResponse getDetailedAnalysis(Student student, List<Project> projects);
    List<Project> generateDynamicProjects(Student student, int count);
}
