package com.projectrecommender.knowledgebase.repository;

import com.projectrecommender.knowledgebase.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * JPA Repository for Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByDomain(String domain);

    List<Project> findByDifficulty(Project.Difficulty difficulty);

    @Query("SELECT p FROM Project p WHERE p.difficulty = :difficulty AND p.domain = :domain")
    List<Project> findByDifficultyAndDomain(@Param("difficulty") Project.Difficulty difficulty,
                                             @Param("domain") String domain);

    @Query("SELECT p FROM Project p WHERE p.skillsRequired LIKE %:skill%")
    List<Project> findBySkillsRequiredContaining(@Param("skill") String skill);

    @Query("SELECT p FROM Project p WHERE p.embeddingVector IS NOT NULL")
    List<Project> findAllWithEmbeddings();

    @Query("SELECT DISTINCT p.domain FROM Project p WHERE p.domain IS NOT NULL")
    List<String> findAllDomains();
}
