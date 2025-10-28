package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.BranchCommissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminBranchCommissionService {

    // CRUD Operations
    Page<BranchCommissionDTO> getAllCommissions(Pageable pageable, String search);
    List<BranchCommissionDTO> getAllCommissions();
    BranchCommissionDTO getCommissionById(Integer commissionId);
    BranchCommissionDTO getActiveCommissionByBranchId(String branchId);
    BranchCommissionDTO createCommission(BranchCommissionDTO dto);
    BranchCommissionDTO updateCommission(Integer commissionId, BranchCommissionDTO dto);
    void deleteCommission(Integer commissionId);

    // Commission Management
    void activateCommission(Integer commissionId);
    void deactivateCommission(Integer commissionId);
    void deactivateOldCommissions(String branchId);
    
    // Statistics
    long getTotalCommissionsCount();
    long getActiveCommissionsCount();
    long getExpiringCommissionsCount();
    long getExpiredCommissionsCount();
    
    // Business Logic
    List<BranchCommissionDTO> getActiveCommissions();
    List<BranchCommissionDTO> getCommissionsByBranch(String branchId);
    List<BranchCommissionDTO> getCommissionsByType(String commissionType);
    List<BranchCommissionDTO> getExpiringCommissions();
    List<BranchCommissionDTO> getExpiredCommissions();
    
    // Commission Calculation
    BigDecimal calculateCommissionForBranch(String branchId, BigDecimal orderTotal, LocalDate orderDate);
    Map<String, Object> getCommissionSummary();
    
    // Validation
    boolean validateCommission(BranchCommissionDTO dto);
    boolean isCommissionOverlapping(String branchId, LocalDate from, LocalDate to, Integer excludeId);
}
