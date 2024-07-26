package com.services;

import com.model.UserEntity;
import com.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.services.iService;

@Service
public class userService implements iService {
    @Autowired
    private userRepository UserRepository;

   @Override
   public UserEntity findByUsername(String username) {
    return UserRepository.findByUsername(username);
   }

}