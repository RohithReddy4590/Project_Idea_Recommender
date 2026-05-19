package com.projectrecommender.infrastructure.adapters.persistence;

import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.ports.outbound.ProjectRepositoryPort;
import com.projectrecommender.infrastructure.adapters.persistence.entities.ProjectEntity;
import com.projectrecommender.infrastructure.adapters.persistence.repositories.JpaProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepositoryPort {

    private final JpaProjectRepository repository;

    @Override
    public List<Project> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Project> findById(Long id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = mapToEntity(project);
        return mapToDomain(repository.save(entity));
    }

    private Project mapToDomain(ProjectEntity entity) {
        return Project.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .difficulty(entity.getDifficulty())
                .domain(entity.getDomain())
                .techStack(entity.getTechStack())
                .skillsRequired(entity.getSkillsRequired())
                .learningOutcomes(entity.getLearningOutcomes())
                .build();
    }

    private ProjectEntity mapToEntity(Project project) {
        return ProjectEntity.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .difficulty(project.getDifficulty())
                .domain(project.getDomain())
                .techStack(project.getTechStack())
                .skillsRequired(project.getSkillsRequired())
                .learningOutcomes(project.getLearningOutcomes())
                .build();
    }
}
