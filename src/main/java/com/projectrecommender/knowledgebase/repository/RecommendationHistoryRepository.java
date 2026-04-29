package com.projectrecommender.knowledgebase.repository;

import com.projectrecommender.knowledgebase.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    List<RecommendationHistory> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<RecommendationHistory> findBySessionId(String sessionId);

    @Query("SELECT rh FROM RecommendationHistory rh WHERE rh.student.id = :studentId AND rh.createdAt > :since")
    List<RecommendationHistory> findRecentByStudentId(@Param("studentId") Long studentId,
                                                       @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM RecommendationHistory rh WHERE rh.createdAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);
}
