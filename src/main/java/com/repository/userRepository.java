package com.repository;

import com.model.UserEntity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Repository
public interface userRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByUserNameAndPassword(String userName, String password);
    UserEntity findByUserName(String userName);
}
