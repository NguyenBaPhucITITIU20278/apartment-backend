package com.services;

import com.model.Contact;
import com.model.Role;
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
import com.repository.ContactRepository;
import com.repository.RoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private RoleRepository roleRepository;

    public UserEntity findUser(String userName) {
        return userRepository.findByUserName(userName);
    }

    public Map<String, String> login(UserEntity user) {
        UserEntity foundUser = userRepository.findByUserName(user.getUserName());
        if (foundUser != null && bCryptPasswordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            if (foundUser.getRole().equals("admin")) {
                String accessToken = jwtUtil.generateToken(foundUser.getUserName());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", "dummyRefreshToken");
                return tokens;
            }
        }
        return null;
    }

    public void deleteUser(String userName) {
        UserEntity user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new RuntimeException("User not found with userName " + userName);
        }
        userRepository.delete(user);
        contactRepository.delete(user.getContact());
        roleRepository.delete(user.getRole());
    }

    public UserEntity updateUser(UserEntity user) {
        UserEntity foundUser = userRepository.findByUserName(user.getUserName());
        if (foundUser == null) {
            throw new RuntimeException("User not found with userName " + user.getUserName());
        }

        // Cập nhật thông tin liên hệ nếu có thay đổi
        Contact contact = foundUser.getContact();
        if (contact != null) {
            contact.setFirstName(user.getContact().getFirstName());
            contact.setLastName(user.getContact().getLastName());
            contact.setPhone(user.getContact().getPhone());
            contactRepository.save(contact);
        } else {
            contact = contactRepository.save(user.getContact());
            foundUser.setContact(contact);
        }

        // Cập nhật thông tin vai trò nếu có thay đổi
        Role role = foundUser.getRole();
        if (role != null) {
            role.setRoleName(user.getRole().getRoleName());
            roleRepository.save(role);
        } else {
            role = roleRepository.save(user.getRole());
            foundUser.setRole(role);
        }

        foundUser.setEmail(user.getEmail());
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        foundUser.setPassword(encodedPassword);
        return userRepository.save(foundUser);
    }
}