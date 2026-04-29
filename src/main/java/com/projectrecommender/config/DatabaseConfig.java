package com.projectrecommender.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database and JPA configuration.
 * Registers the shared ObjectMapper with Java 8 date/time support.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.projectrecommender.knowledgebase.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Shared ObjectMapper with Java 8 date/time support.
     * Used by LLMService and EmbeddingService for JSON parsing.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
