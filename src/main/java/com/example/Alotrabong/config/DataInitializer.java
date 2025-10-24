package com.example.Alotrabong.config;

import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        // Tạo các role cơ bản nếu chưa có
        createRoleIfNotExists(RoleCode.ADMIN, "Administrator");
        createRoleIfNotExists(RoleCode.BRANCH_MANAGER, "Branch Manager");
        createRoleIfNotExists(RoleCode.SHIPPER, "Shipper");
        createRoleIfNotExists(RoleCode.USER, "User");
        
        log.info("Role initialization completed");
    }

    private void createRoleIfNotExists(RoleCode roleCode, String roleName) {
        if (!roleRepository.findByRoleCode(roleCode).isPresent()) {
            Role role = Role.builder()
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {} - {}", roleCode, roleName);
        } else {
            log.debug("Role {} already exists", roleCode);
        }
    }
}
