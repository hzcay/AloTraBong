package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.BranchDTO;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public BranchDTO createBranch(BranchDTO branchDTO) {
        log.info("Creating new branch: {}", branchDTO.getName());
        
        Branch branch = Branch.builder()
                .name(branchDTO.getName())
                .address(branchDTO.getAddress())
                .phone(branchDTO.getPhone())
                .isActive(true)
                .build();
        
        branch = branchRepository.save(branch);
        log.info("Branch created successfully: {}", branch.getBranchId());
        
        return convertToDTO(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDTO getBranchById(String branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        return convertToDTO(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDTO updateBranch(String branchId, BranchDTO branchDTO) {
        log.info("Updating branch: {}", branchId);
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        branch.setName(branchDTO.getName());
        branch.setAddress(branchDTO.getAddress());
        branch.setPhone(branchDTO.getPhone());
        
        branch = branchRepository.save(branch);
        log.info("Branch updated successfully: {}", branchId);
        
        return convertToDTO(branch);
    }

    @Override
    public void deleteBranch(String branchId) {
        log.info("Deleting branch: {}", branchId);
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        branch.setIsActive(false);
        branchRepository.save(branch);
        
        log.info("Branch deactivated: {}", branchId);
    }

    private BranchDTO convertToDTO(Branch branch) {
        return BranchDTO.builder()
                .branchId(branch.getBranchId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
