package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.UserManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminUserService {

    // User Account Management
    Page<UserManagementDTO> getAllUsers(Pageable pageable, String search);

    UserManagementDTO getUserById(String userId);

    void lockUser(String userId);

    void unlockUser(String userId);

    void resetUserPassword(String userId, String newPassword);

    void assignRoleToUser(String userId, String roleCode);

    void removeRoleFromUser(String userId, String roleCode);

    List<UserManagementDTO> getUsersByRole(String roleCode);

    void deleteUser(String userId);

    UserManagementDTO updateUserInfo(String userId, UserManagementDTO dto);

    long getTotalUsersCount();

    long getActiveUsersCount();

    long getLockedUsersCount();
}