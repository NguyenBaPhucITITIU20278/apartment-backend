package com.services;

import com.model.UserEntity;
import com.repository.UserRepository;
import com.security.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    public UserEntity updateUser(String id, UserEntity userDetails) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));
        user.setUserName(userDetails.getUserName());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));
        userRepository.delete(user);
    }

    public Map<String, String> login(UserEntity user) {
        UserEntity foundUser = userRepository.findByIdAndPassword(user.getId(), user.getPassword());
        if (foundUser != null) {
            String accessToken = jwtUtil.generateToken(foundUser.getId());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", "dummyRefreshToken"); 
            return tokens;
        }
        return null;
    }

    public UserEntity getUser(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}
