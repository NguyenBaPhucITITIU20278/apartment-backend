package com.repository;

import com.model.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepository extends JpaRepository<user, Long> {
    user findByUsername(String username);
}