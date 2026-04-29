package com.projectrecommender.knowledgebase.service;

import com.projectrecommender.knowledgebase.entity.Skill;
import com.projectrecommender.knowledgebase.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages skill taxonomy and relationships.
 * Provides hierarchical skill lookup and related skill resolution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillTaxonomyService {

    private final SkillRepository skillRepository;

    /**
     * Get all skills for a given category.
     */
    public List<Skill> getSkillsByCategory(String category) {
        return skillRepository.findByCategory(category);
    }

    /**
     * Find a skill by name (case-insensitive).
     */
    public Optional<Skill> findSkillByName(String name) {
        return skillRepository.findByNameIgnoreCase(name);
    }

    /**
     * Get all categories available in the taxonomy.
     */
    public List<String> getAllCategories() {
        return skillRepository.findAll().stream()
                .map(Skill::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get child skills for a given parent skill.
     */
    public List<Skill> getChildSkills(String parentSkillName) {
        return skillRepository.findByParentSkill(parentSkillName);
    }

    /**
     * Normalize a list of skill name strings into known skill entities.
     * Skills not found are returned as-is in a separate list.
     */
    public Map<String, Object> normalizeSkills(List<String> skillNames) {
        List<Skill> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String name : skillNames) {
            Optional<Skill> skill = skillRepository.findByNameIgnoreCase(name.trim());
            if (skill.isPresent()) {
                matched.add(skill.get());
            } else {
                unmatched.add(name.trim());
            }
        }

        return Map.of("matched", matched, "unmatched", unmatched);
    }

    /**
     * Determine if two skills are related (same category or parent-child).
     */
    public boolean areSkillsRelated(String skill1, String skill2) {
        Optional<Skill> s1 = skillRepository.findByNameIgnoreCase(skill1);
        Optional<Skill> s2 = skillRepository.findByNameIgnoreCase(skill2);

        if (s1.isEmpty() || s2.isEmpty()) return false;

        Skill a = s1.get();
        Skill b = s2.get();

        // Same category
        if (a.getCategory() != null && a.getCategory().equalsIgnoreCase(b.getCategory())) return true;

        // Parent-child
        if (a.getName().equalsIgnoreCase(b.getParentSkill())) return true;
        if (b.getName().equalsIgnoreCase(a.getParentSkill())) return true;

        return false;
    }
}
