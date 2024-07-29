package com.controller;

import com.model.UserEntity;
import com.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();   
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity user) {
        try {
            if (user.getUserName() == null || user.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
            }
            if(userService.login(user) != null) {
                return ResponseEntity.ok(userService.login(user));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    // @PostMapping("/create")
    // public UserEntity createUser(@RequestBody UserEntity user) {
    //     return userService.createUser(user);
    // }

    // @PutMapping("/update/{id}")
    // public UserEntity updateUser(@PathVariable String id, @RequestBody UserEntity userDetails) {
    //     return userService.updateUser(id, userDetails);
    // }

    // @DeleteMapping("/delete/{id}")
    // public void deleteUser(@PathVariable String id) {
    //     userService.deleteUser(id);
    // }
}
