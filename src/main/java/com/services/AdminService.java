package com.services;

import com.model.UserEntity;
import com.repository.userRepository;
import com.security.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.repository.adRepository;


@EnableAutoConfiguration
@Configuration
@ComponentScan
@Service
public class AdminService {

    @Autowired
    private adRepository AdRepository;
    @Autowired
    private JwtUtil jwtUtil;

    
    public UserEntity findUser(String userName) {
        return AdRepository.findByUserName(userName);
    }
}