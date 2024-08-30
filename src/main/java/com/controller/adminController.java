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
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/loginAdmin")
    public ResponseEntity<?> login(@RequestBody UserEntity user) {
        try {
            if (user.getUserName() == null || user.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
            }

            UserEntity existingUser = adminService.findUser(user.getUserName());
            if (existingUser == null
                    || !bCryptPasswordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }

            if (!existingUser.getRole().getRoleName().equals("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            String accessToken = jwtUtil.generateToken(existingUser.getUserName());
            String refreshToken = jwtUtil.generateRefreshToken(existingUser.getUserName());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/find-user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token, @RequestBody UserEntity user) {
        String userName = user.getUserName();
        String accessToken = token.replace("Bearer ", "");
        try {
            if (userName == null || accessToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid token");
            } else {
                UserEntity foundUser = adminService.findUser(userName);
                if (foundUser == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
                return ResponseEntity.status(HttpStatus.OK).body(foundUser);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Transactional
    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteUser(@RequestBody UserEntity user) {
        try {
            String userName = user.getUserName();
            System.out.println(userName);
            adminService.deleteUser(userName);
            return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserEntity user) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(adminService.updateUser(user));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
