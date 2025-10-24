package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.UserManagementDTO;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.RoleRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import com.example.Alotrabong.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserManagementDTO> getAllUsers(Pageable pageable, String search) {
        log.info("Fetching all users with search: {}", search);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDTO);
    }

    @Override
    public UserManagementDTO getUserById(String userId) {
        log.info("Fetching user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return convertToDTO(user);
    }

    @Override
    public void lockUser(String userId) {
        log.info("Locking user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(String userId) {
        log.info("Unlocking user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    public void resetUserPassword(String userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void assignRoleToUser(String userId, String roleCode) {
        log.info("Assigning role {} to user: {}", roleCode, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RoleCode code = RoleCode.valueOf(roleCode);
        Role role = roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));

        boolean alreadyHasRole = userRoleRepository.existsByUserAndRole(user, role);
        if (alreadyHasRole) {
            throw new BadRequestException("User already has this role");
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleCode) {
        log.info("Removing role {} from user: {}", roleCode, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RoleCode code = RoleCode.valueOf(roleCode);
        Role role = roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));

        userRoleRepository.deleteByUserAndRole(user, role);
    }

    @Override
    public List<UserManagementDTO> getUsersByRole(String roleCode) {
        log.info("Fetching users by role: {}", roleCode);
        RoleCode code = RoleCode.valueOf(roleCode);
        Role role = roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));

        List<UserRole> userRoles = userRoleRepository.findByRole(role);
        return userRoles.stream()
                .map(ur -> convertToDTO(ur.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRoleRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    public UserManagementDTO updateUserInfo(String userId, UserManagementDTO dto) {
        log.info("Updating user info: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public long getActiveUsersCount() {
        return userRepository.countByIsActive(true);
    }

    @Override
    public long getLockedUsersCount() {
        return userRepository.countByIsActive(false);
    }

    private UserManagementDTO convertToDTO(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getRoleCode().toString())
                .collect(Collectors.toList());

        return UserManagementDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .build();
    }
}

