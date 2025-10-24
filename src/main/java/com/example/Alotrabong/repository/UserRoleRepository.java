package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
    void deleteByUser(User user);
    void deleteByUserAndRole(User user, Role role);
    boolean existsByUserAndRole(User user, Role role);
}
