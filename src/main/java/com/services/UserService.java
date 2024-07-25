package com.services;

import com.model.user;
import com.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class UserService {
    
    @Autowired
    private userRepository userRepository;

    public user createUser(user user) {
        return userRepository.save(user);
    }

    public user getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public user updateUser(Long id, user userDetails) {
        user user = getUserById(id);
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        user user = getUserById(id);
        userRepository.delete(user);
    }
}