package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.BranchDTO;

import java.util.List;

public interface BranchService {
    
    BranchDTO createBranch(BranchDTO branchDTO);
    
    BranchDTO getBranchById(String branchId);
    
    List<BranchDTO> getAllBranches();
    
    BranchDTO updateBranch(String branchId, BranchDTO branchDTO);
    
    void deleteBranch(String branchId);
}
