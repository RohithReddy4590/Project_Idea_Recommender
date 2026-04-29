package com.projectrecommender;

import com.projectrecommender.config.AgentConfig;
import com.projectrecommender.config.OpenAIConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Project Idea Recommender Agent application.
 * An AI agent system that recommends portfolio projects to students based on
 * their skills, interests, and career goals using hybrid AI reasoning.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({OpenAIConfig.class, AgentConfig.class})
public class ProjectRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectRecommenderApplication.class, args);
    }
}
