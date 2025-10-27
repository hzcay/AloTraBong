package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.BranchManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AdminBranchService {

    // Branch Management
    Page<BranchManagementDTO> getAllBranches(Pageable pageable, String search);

    BranchManagementDTO getBranchById(String branchId);

    BranchManagementDTO createBranch(BranchManagementDTO dto);

    BranchManagementDTO updateBranch(String branchId, BranchManagementDTO dto);

    void activateBranch(String branchId);

    void deactivateBranch(String branchId);

    void assignBranchManager(String branchId, String userId);

    void removeBranchManager(String branchId);

    long getTotalBranchesCount();

    long getActiveBranchesCount();

    List<BranchManagementDTO> getAllBranchesForDropdown();
}

