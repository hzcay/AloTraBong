package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.BranchManagementDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import com.example.Alotrabong.repository.RoleRepository;
import com.example.Alotrabong.service.AdminBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminBranchServiceImpl implements AdminBranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public Page<BranchManagementDTO> getAllBranches(Pageable pageable, String search) {
        log.info("Fetching all branches with search: {}", search);
        Page<Branch> branches = branchRepository.findAll(pageable);
        return branches.map(this::convertToDTO);
    }

    @Override
    public BranchManagementDTO getBranchById(String branchId) {
        log.info("Fetching branch by id: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return convertToDTO(branch);
    }

    @Override
    public BranchManagementDTO createBranch(BranchManagementDTO dto) {
        log.info("Creating new branch: {}", dto.getName());
        Branch branch = Branch.builder()
                .branchCode(dto.getBranchCode())
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .district(dto.getDistrict())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .isActive(true)
                .openHours(dto.getOpenHours())
                .build();

        branch = branchRepository.save(branch);
        return convertToDTO(branch);
    }

    @Override
    public BranchManagementDTO updateBranch(String branchId, BranchManagementDTO dto) {
        log.info("Updating branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        branch.setName(dto.getName());
        branch.setAddress(dto.getAddress());
        branch.setPhone(dto.getPhone());
        branch.setDistrict(dto.getDistrict());
        branch.setCity(dto.getCity());
        branch.setLatitude(dto.getLatitude());
        branch.setLongitude(dto.getLongitude());
        branch.setOpenHours(dto.getOpenHours());

        branch = branchRepository.save(branch);
        return convertToDTO(branch);
    }

    @Override
    public void activateBranch(String branchId) {
        log.info("Activating branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        branch.setIsActive(true);
        branchRepository.save(branch);
    }

    @Override
    public void deactivateBranch(String branchId) {
        log.info("Deactivating branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        branch.setIsActive(false);
        branchRepository.save(branch);
    }

    @Override
    public void assignBranchManager(String branchId, String userId) {
        log.info("Assigning branch manager {} to branch: {}", userId, branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get BRANCH_MANAGER role
        Role managerRole = roleRepository.findByRoleCode(RoleCode.BRANCH_MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("BRANCH_MANAGER role not found"));

        // Find user's existing BRANCH_MANAGER role (user must already have this role)
        UserRole userManagerRole = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getRoleCode() == RoleCode.BRANCH_MANAGER)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User does not have BRANCH_MANAGER role"));

        // Check if this branch already has a manager
        Optional<UserRole> existingManager = userRoleRepository.findManagerByBranchId(branchId, RoleCode.BRANCH_MANAGER);
        if (existingManager.isPresent() && !existingManager.get().getUser().getUserId().equals(userId)) {
            // Remove branch assignment from previous manager (set branch to null)
            UserRole oldManager = existingManager.get();
            oldManager.setBranch(null);
            userRoleRepository.save(oldManager);
            log.info("Removed branch assignment from previous manager: {}", oldManager.getUser().getUserId());
        }

        // Update existing UserRole: assign this branch to the manager
        // KHÔNG tạo UserRole mới, CHỈ cập nhật branch_id
        userManagerRole.setBranch(branch);
        userRoleRepository.save(userManagerRole);
        log.info("Successfully assigned manager {} to branch: {}", userId, branchId);
    }

    @Override
    public void removeBranchManager(String branchId) {
        log.info("Removing branch manager from branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        // Tìm manager hiện tại của chi nhánh
        Optional<UserRole> managerRole = userRoleRepository.findManagerByBranchId(branchId, RoleCode.BRANCH_MANAGER);

        if (managerRole.isPresent()) {
            // CHỈ set branch = NULL, KHÔNG xóa UserRole (giữ lại role BRANCH_MANAGER)
            UserRole userRole = managerRole.get();
            userRole.setBranch(null);
            userRoleRepository.save(userRole);
            log.info("Successfully removed branch assignment from manager: {}", userRole.getUser().getUserId());
        } else {
            log.warn("No manager found for branch: {}", branchId);
        }
    }

    @Override
    public long getTotalBranchesCount() {
        return branchRepository.count();
    }

    @Override
    public long getActiveBranchesCount() {
        return branchRepository.countByIsActive(true);
    }

    private BranchManagementDTO convertToDTO(Branch branch) {
        BranchManagementDTO.BranchManagementDTOBuilder builder = BranchManagementDTO.builder()
                .branchId(branch.getBranchId())
                .branchCode(branch.getBranchCode())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .district(branch.getDistrict())
                .city(branch.getCity())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .isActive(branch.getIsActive())
                .openHours(branch.getOpenHours())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt());

        // Add manager information if exists
        Optional<UserRole> managerRole = userRoleRepository.findManagerByBranchId(
                branch.getBranchId(),
                RoleCode.BRANCH_MANAGER
        );

        if (managerRole.isPresent()) {
            User manager = managerRole.get().getUser();
            builder.managerId(manager.getUserId())
                   .managerName(manager.getFullName())
                   .managerEmail(manager.getEmail())
                   .managerPhone(manager.getPhone());
        }

        return builder.build();
    }
}

