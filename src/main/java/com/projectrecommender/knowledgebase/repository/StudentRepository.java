package com.projectrecommender.knowledgebase.repository;

import com.projectrecommender.knowledgebase.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.studentSkills ss LEFT JOIN FETCH ss.skill WHERE s.id = :id")
    Optional<Student> findByIdWithSkills(@Param("id") Long id);
}
