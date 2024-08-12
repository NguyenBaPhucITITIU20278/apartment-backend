package com.controller;

import com.model.UserEntity;
import com.services.UserService;
import com.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.services.AdminService;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")

public class adminController {
    private static final Logger logger = LoggerFactory.getLogger(adminController.class);

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/find-user")
public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token, @RequestBody UserEntity user) {
    String userName = user.getUserName();
    String accessToken = token.replace("Bearer ", "");    
    try {
        if (userName == null || accessToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid token");
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(adminService.findUser(userName));
        }
    } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}

