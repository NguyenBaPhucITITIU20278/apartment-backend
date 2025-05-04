package com.repository;

import com.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByUserNameAndPassword(String userName, String password);
    UserEntity findByUserName(String userName);
    void deleteByUserName(String userName);
    UserEntity findByEmail(String email);
    List<UserEntity> findByRoleId(Long roleId);
}
