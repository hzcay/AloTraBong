package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAssignmentService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Lấy branchId của user từ authentication
     * @param authentication Spring Security authentication object
     * @return branchId nếu user được assign, null nếu chưa được assign
     */
    public String getBranchIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Authentication is null or user name is null");
            return null;
        }

        try {
            // Lấy user từ email/username
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

            // Tìm UserRole với role BRANCH_MANAGER bằng RoleCode và đảm bảo đã có branch
            Optional<UserRole> branchManagerRole = userRoleRepository
                    .findFirstByUser_UserIdAndRole_RoleCodeAndBranchIsNotNull(user.getUserId(), RoleCode.BRANCH_MANAGER);

            if (branchManagerRole.isEmpty()) {
                log.warn("User {} does not have BRANCH_MANAGER role", user.getEmail());
                return null;
            }

            UserRole userRole = branchManagerRole.get();
            
            // Kiểm tra xem có được assign branch không
            if (userRole.getBranch() == null) {
                log.warn("User {} has BRANCH_MANAGER role but is not assigned to any branch", user.getEmail());
                return null;
            }

            String branchId = userRole.getBranch().getBranchId();
            log.info("User {} is assigned to branch: {}", user.getEmail(), branchId);
            return branchId;

        } catch (Exception e) {
            log.error("Error getting branch ID for user {}: {}", authentication.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra user có phải Branch Manager không
     */
    public boolean isBranchManager(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

            return userRoleRepository
                    .findFirstByUser_UserIdAndRole_RoleCodeAndBranchIsNotNull(user.getUserId(), RoleCode.BRANCH_MANAGER)
                    .isPresent();
        } catch (Exception e) {
            log.error("Error checking BRANCH_MANAGER role for user {}: {}", authentication.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra user có được assign branch không
     */
    public boolean hasBranchAssignment(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        return branchId != null;
    }
}
