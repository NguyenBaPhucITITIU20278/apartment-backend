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



import java.util.Map;
import java.util.HashMap;
import java.util.List;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class usercontroller {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/all")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity user) {
        try {
            if (user.getUserName() == null || user.getPassword() == null) {
                System.out.println("Missing username or password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
            }
            Map<String, String> tokens = userService.login(user);
            if (tokens != null) {
                return ResponseEntity.status(HttpStatus.OK).body(tokens);
            } else {
                System.out.println("Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    @PostMapping("/create")
    public UserEntity createUser(@RequestBody UserEntity user) {
        return userService.createUser(user);
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@RequestHeader("token") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid token");
            }
            String jwtToken = token.substring(7);
            String userName = jwtUtil.extractUserName(jwtToken);
            UserEntity user = userService.getUser(userName);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");

        }
    }

    
}