package com.repository;

import com.model.Otp;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@EnableAutoConfiguration
@Configuration
@ComponentScan
@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {
    Otp findByOtpAndEmail(String otp, String email);
    void deleteByOtp(String otp);
}
