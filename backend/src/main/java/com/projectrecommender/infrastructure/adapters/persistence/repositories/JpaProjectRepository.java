package com.projectrecommender.infrastructure.adapters.persistence.repositories;

import com.projectrecommender.infrastructure.adapters.persistence.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProjectRepository extends JpaRepository<ProjectEntity, Long> {
}
