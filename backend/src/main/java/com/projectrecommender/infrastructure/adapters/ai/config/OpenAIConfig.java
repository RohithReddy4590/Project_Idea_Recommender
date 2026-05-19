package com.projectrecommender.infrastructure.adapters.ai.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
