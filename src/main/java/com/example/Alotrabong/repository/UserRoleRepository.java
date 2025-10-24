package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    void deleteByUser(User user);
}
