package com.projectrecommender.infrastructure.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectrecommender.core.domain.models.Project;
import com.projectrecommender.core.ports.outbound.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProjectRepositoryPort projectRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (projectRepository.findAll().isEmpty()) {
            InputStream inputStream = new ClassPathResource("seed_projects.json").getInputStream();
            List<Project> projects = objectMapper.readValue(inputStream, new TypeReference<List<Project>>() {});
            for (Project project : projects) {
                projectRepository.save(project);
            }
            System.out.println("Seeded " + projects.size() + " projects.");
        }
    }
}
