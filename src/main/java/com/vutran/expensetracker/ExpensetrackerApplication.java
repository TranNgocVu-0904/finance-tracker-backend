package com.vutran.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ExpensetrackerApplication {

    public static void main(String[] args) {
        try {
            // 1. Initialize and configure Dotenv to load environment variables
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Specify the root directory as the source for the .env file
                    .ignoreIfMissing() // Prevent application failure if the .env file is absent in production
                    .load();

            // 2. Map loaded environment variables to System Properties
            // This ensures variables are available during the Spring Boot bootstrap process
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                
                // Optional: Log mail-related configuration for verification during initialization
                if (entry.getKey().contains("MAIL")) {
                    System.out.println("Loaded environment variable: " + entry.getKey());
                }
            });
        } catch (Exception e) {
            // Log a warning if the .env file is missing; the system will fall back to environment-level variables
            System.out.println("Warning: .env file not detected. Proceeding with default system environment variables.");
        }

        // 3. Launch the Spring Boot application
        SpringApplication.run(ExpensetrackerApplication.class, args);
    }
}