package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.BranchManagementDTO;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.service.AdminBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminBranchServiceImpl implements AdminBranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

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
    }

    @Override
    public void removeBranchManager(String branchId) {
        log.info("Removing branch manager from branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
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
        return BranchManagementDTO.builder()
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
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}

