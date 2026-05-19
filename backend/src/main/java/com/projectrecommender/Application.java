package com.projectrecommender;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("..")
                .load();
        
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            if (entry.getKey().equalsIgnoreCase("GEMINI_API_KEY")) {
                System.setProperty("gemini.api.key", entry.getValue());
            }
        });

        String key = System.getProperty("GEMINI_API_KEY");
        if (key == null || key.isEmpty()) {
            System.out.println("CRITICAL: .env file found but GEMINI_API_KEY is EMPTY!");
        } else {
            System.out.println("SUCCESS: Loaded Gemini Key (Starts with: " + key.substring(0, 7) + "...)");
        }

        SpringApplication.run(Application.class, args);
    }
}
