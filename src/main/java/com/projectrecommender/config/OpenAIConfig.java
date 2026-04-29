package com.projectrecommender.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI API configuration properties.
 * Reads from application.yml under the 'openai' prefix.
 */
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAIConfig {

    private String apiKey;
    private String model = "gpt-4o-mini";
    private String embeddingModel = "text-embedding-3-small";
    private Integer maxTokens = 2048;
    private Double temperature = 0.7;
    private Integer timeout = 60;
}
