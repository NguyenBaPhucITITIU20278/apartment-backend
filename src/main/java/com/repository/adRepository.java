package com.repository;

import com.model.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface adRepository extends JpaRepository<UserEntity, Long> { // Đảm bảo kiểu ID khớp với entity Room
   UserEntity findByUserName(String userName);
}