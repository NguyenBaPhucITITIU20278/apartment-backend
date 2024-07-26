package com.services;

import com.model.UserEntity;

public interface iService {
    UserEntity findByUsername(String username);
}
