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
import com.repository.userRepository;



@EnableAutoConfiguration
@Configuration
@ComponentScan
@Service
public class AdminService {

    @Autowired
    private adRepository AdRepository;
    @Autowired
    private userRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    
    public UserEntity findUser(String userName) {
        return AdRepository.findByUserName(userName);
    }
    public Map<String, String> login(UserEntity user) {
        UserEntity foundUser = userRepository.findByUserNameAndPassword(user.getUserName(), user.getPassword());
        System.out.println(foundUser.getRole());
        if(foundUser.getRole().equals("admin")){
            if (foundUser != null) {
                String accessToken = jwtUtil.generateToken(foundUser.getUserName());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", "dummyRefreshToken");
                return tokens;
            }
        }
        return null;
    }
    public String deleteUser(String userName) {
        userRepository.deleteByUserName(userName);
        return "User deleted successfully";
    }
    public String updateUser(UserEntity user) {
        userRepository.save(user);
        return "User updated successfully";
    }
}