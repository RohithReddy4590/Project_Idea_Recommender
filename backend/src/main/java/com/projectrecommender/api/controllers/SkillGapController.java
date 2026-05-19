package com.projectrecommender.api.controllers;

import com.projectrecommender.api.dto.request.RecommendationRequest;
import com.projectrecommender.api.dto.response.SkillGapResponse;
import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.domain.models.Student;
import com.projectrecommender.core.ports.outbound.OpenAIServicePort;
import com.projectrecommender.core.ports.outbound.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skill-gap")
@RequiredArgsConstructor
public class SkillGapController {

    private final OpenAIServicePort openAIService;
    private final ProjectRepositoryPort projectRepository;

    @PostMapping
    public SkillGapResponse analyzeSkillGap(@RequestBody RecommendationRequest request) {
        // Simplified for standalone analysis
        Student student = Student.builder()
                .skills(request.getSkills())
                .careerGoal(request.getGoal())
                .build();
        
        List<Project> all = projectRepository.findAll();
        if (all.isEmpty()) return new SkillGapResponse();

        Map<Long, List<String>> gaps = openAIService.getSkillGaps(student, all.subList(0, Math.min(3, all.size())));
        
        return SkillGapResponse.builder()
                .gaps(gaps)
                .build();
    }
}
