package com.projectrecommender.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent behavior configuration.
 * Controls recommendation limits, scoring weights, and memory settings.
 */
@ConfigurationProperties(prefix = "agent")
@Data
public class AgentConfig {

    private int maxRecommendations = 10;
    private double minSkillMatchScore = 0.3;
    private double rankingWeightSkill = 0.4;
    private double rankingWeightSemantic = 0.3;
    private double rankingWeightLlm = 0.3;
    private int memoryTtlDays = 30;
}
