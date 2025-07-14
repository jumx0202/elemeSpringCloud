package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.entity")
@EnableJpaRepositories("org.example.mapper")
public class ElemeSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElemeSpringBootApplication.class, args);
    }
} 