package com.projectrecommender.api.controller;

import com.projectrecommender.api.dto.request.ProfileRequest;
import com.projectrecommender.knowledgebase.entity.Skill;
import com.projectrecommender.knowledgebase.entity.Student;
import com.projectrecommender.knowledgebase.entity.StudentSkill;
import com.projectrecommender.knowledgebase.repository.SkillRepository;
import com.projectrecommender.knowledgebase.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

/**
 * REST controller for managing student profiles.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final StudentRepository studentRepository;
    private final SkillRepository   skillRepository;

    /**
     * POST /profile/create
     * Create a new student profile with skills.
     */
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<Student> createProfile(@Valid @RequestBody ProfileRequest request) {
        if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A profile with this email already exists: " + request.getEmail());
        }

        // 1. Build and save student first (to get generated ID)
        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .careerGoal(request.getCareerGoal())
                .domainInterests(request.getDomainInterests())
                .experienceLevel(request.getExperienceLevel())
                .studentSkills(new HashSet<>())
                .build();

        Student savedStudent = studentRepository.save(student);

        // 2. Save each skill linked to the saved student
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            for (ProfileRequest.SkillEntry entry : request.getSkills()) {
                Skill skill = skillRepository.findByNameIgnoreCase(entry.getSkillName())
                        .orElseGet(() -> {
                            Skill newSkill = Skill.builder()
                                    .name(entry.getSkillName())
                                    .category("General")
                                    .difficultyLevel(1)
                                    .studentSkills(new HashSet<>())
                                    .build();
                            return skillRepository.save(newSkill);
                        });

                StudentSkill ss = StudentSkill.builder()
                        .student(savedStudent)
                        .skill(skill)
                        .proficiencyLevel(entry.getProficiencyLevel() != null
                                ? entry.getProficiencyLevel()
                                : StudentSkill.ProficiencyLevel.INTERMEDIATE)
                        .yearsExperience(entry.getYearsExperience() != null
                                ? entry.getYearsExperience()
                                : 1.0)
                        .build();
                savedStudent.getStudentSkills().add(ss);
            }
            studentRepository.save(savedStudent);
        }

        log.info("Profile created: {} (id={})", savedStudent.getName(), savedStudent.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    /**
     * GET /profile/{id}
     * Retrieve a student profile by ID (includes skills).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getProfile(@PathVariable Long id) {
        Student student = studentRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        return ResponseEntity.ok(student);
    }

    /**
     * PUT /profile/{id}
     * Update an existing student profile.
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Student> updateProfile(@PathVariable Long id,
                                                  @Valid @RequestBody ProfileRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        student.setName(request.getName());
        student.setCareerGoal(request.getCareerGoal());
        student.setDomainInterests(request.getDomainInterests());
        student.setExperienceLevel(request.getExperienceLevel());

        return ResponseEntity.ok(studentRepository.save(student));
    }

    /**
     * GET /profile
     * List all student profiles.
     */
    @GetMapping
    public ResponseEntity<List<Student>> listProfiles() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    /**
     * DELETE /profile/{id}
     * Delete a student profile.
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}