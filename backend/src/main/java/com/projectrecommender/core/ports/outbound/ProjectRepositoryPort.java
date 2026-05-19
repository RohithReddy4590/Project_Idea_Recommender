package com.projectrecommender.core.ports.outbound;

import com.projectrecommender.core.domain.models.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectRepositoryPort {
    List<Project> findAll();
    Optional<Project> findById(Long id);
    Project save(Project project);
}
