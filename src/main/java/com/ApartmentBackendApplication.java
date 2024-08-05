package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;


@EnableAutoConfiguration
@Configuration
@ComponentScan
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.repository")
@EntityScan(basePackages = "com.model")
public class ApartmentBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApartmentBackendApplication.class, args);
    }
}