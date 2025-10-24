package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.repository.RoleRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/create-test-users")
    public String createTestUsers() {
        try {
            // Tạo ADMIN user
            createUserIfNotExists("admin@test.com", "admin123", "Admin User", "0123456789", RoleCode.ADMIN);
            
            // Tạo BRANCH_MANAGER user
            createUserIfNotExists("branch@test.com", "branch123", "Branch Manager", "0123456788", RoleCode.BRANCH_MANAGER);
            
            // Tạo SHIPPER user
            createUserIfNotExists("shipper@test.com", "shipper123", "Shipper User", "0123456787", RoleCode.SHIPPER);
            
            log.info("Test users created successfully!");
            return "redirect:/auth?success=Test users created successfully!";
            
        } catch (Exception e) {
            log.error("Error creating test users: {}", e.getMessage());
            return "redirect:/auth?error=Error creating test users: " + e.getMessage();
        }
    }

    @GetMapping("/create-users")
    public String createUsersPage() {
        return "test/create-users";
    }

    private void createUserIfNotExists(String email, String password, String fullName, String phone, RoleCode roleCode) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("User {} already exists, skipping...", email);
            return;
        }

        // Create user
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .isActive(true) // Active immediately for testing
                .build();

        user = userRepository.save(user);

        // Get role
        Optional<Role> roleOpt = roleRepository.findByRoleCode(roleCode);
        if (roleOpt.isEmpty()) {
            log.error("Role {} not found in database", roleCode);
            return;
        }

        // Assign role to user
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(roleOpt.get())
                .build();

        userRoleRepository.save(userRole);
        
        log.info("Created user: {} with role: {}", email, roleCode);
    }
}
