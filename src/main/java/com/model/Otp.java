package com.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@EnableAutoConfiguration
@Configuration
@ComponentScan
@Data
@Entity
@Table(name = "otp_schema", schema = "public")
public class Otp {
    
    @Id
    @Column(name = "otp")
    private String otp;

    @Column(name = "email")
    private String email;

    @Column(name = "createdAt")
    private String createAt;
}

