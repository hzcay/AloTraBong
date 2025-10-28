package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.BranchCommissionDTO;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.BranchCommission;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.BranchCommissionRepository;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.service.AdminBranchCommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminBranchCommissionServiceImpl implements AdminBranchCommissionService {

    private final BranchCommissionRepository commissionRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BranchCommissionDTO> getAllCommissions(Pageable pageable, String search) {
        log.info("Fetching all commissions with search: {}", search);
        Page<BranchCommission> commissions;
        
        if (search != null && !search.trim().isEmpty()) {
            // For now, we'll search by branch name - can be enhanced later
            commissions = commissionRepository.findAll(pageable);
        } else {
            commissions = commissionRepository.findAll(pageable);
        }
        
        return commissions.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getAllCommissions() {
        log.info("Fetching all commissions");
        List<BranchCommission> commissions = commissionRepository.findAll();
        return commissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BranchCommissionDTO getCommissionById(Integer commissionId) {
        log.info("Fetching commission by id: {}", commissionId);
        BranchCommission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + commissionId));
        return convertToDTO(commission);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchCommissionDTO getActiveCommissionByBranchId(String branchId) {
        log.info("Fetching active commission for branch: {}", branchId);
        BranchCommission commission = commissionRepository.findActiveCommissionByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("No active commission found for branch: " + branchId));
        return convertToDTO(commission);
    }

    @Override
    public BranchCommissionDTO createCommission(BranchCommissionDTO dto) {
        log.info("Creating new commission for branch: {}", dto.getBranchId());
        
        // Validate commission
        if (!validateCommission(dto)) {
            throw new IllegalArgumentException("Invalid commission data");
        }
        
        // Check for overlapping commissions
        if (isCommissionOverlapping(dto.getBranchId(), dto.getEffectiveFrom(), dto.getEffectiveTo(), null)) {
            throw new IllegalArgumentException("Commission period overlaps with existing active commission");
        }
        
        // Deactivate old commissions for this branch
        deactivateOldCommissions(dto.getBranchId());
        
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + dto.getBranchId()));
        
        BranchCommission commission = BranchCommission.builder()
                .branch(branch)
                .commissionType(dto.getCommissionType())
                .commissionValue(dto.getCommissionValue())
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .note(dto.getNote())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        commission = commissionRepository.save(commission);
        return convertToDTO(commission);
    }

    @Override
    public BranchCommissionDTO updateCommission(Integer commissionId, BranchCommissionDTO dto) {
        log.info("Updating commission: {}", commissionId);
        BranchCommission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + commissionId));

        // Validate commission
        if (!validateCommission(dto)) {
            throw new IllegalArgumentException("Invalid commission data");
        }
        
        // Check for overlapping commissions (excluding current one)
        if (isCommissionOverlapping(dto.getBranchId(), dto.getEffectiveFrom(), dto.getEffectiveTo(), commissionId)) {
            throw new IllegalArgumentException("Commission period overlaps with existing active commission");
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + dto.getBranchId()));

        commission.setBranch(branch);
        commission.setCommissionType(dto.getCommissionType());
        commission.setCommissionValue(dto.getCommissionValue());
        commission.setEffectiveFrom(dto.getEffectiveFrom());
        commission.setEffectiveTo(dto.getEffectiveTo());
        commission.setNote(dto.getNote());
        commission.setIsActive(dto.getIsActive());

        commission = commissionRepository.save(commission);
        return convertToDTO(commission);
    }

    @Override
    public void deleteCommission(Integer commissionId) {
        log.info("Deleting commission: {}", commissionId);
        BranchCommission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + commissionId));
        commissionRepository.delete(commission);
    }

    @Override
    public void activateCommission(Integer commissionId) {
        log.info("Activating commission: {}", commissionId);
        BranchCommission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + commissionId));
        
        // Deactivate other commissions for the same branch
        deactivateOldCommissions(commission.getBranch().getBranchId());
        
        commission.setIsActive(true);
        commissionRepository.save(commission);
    }

    @Override
    public void deactivateCommission(Integer commissionId) {
        log.info("Deactivating commission: {}", commissionId);
        BranchCommission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + commissionId));
        commission.setIsActive(false);
        commissionRepository.save(commission);
    }

    @Override
    public void deactivateOldCommissions(String branchId) {
        log.info("Deactivating old commissions for branch: {}", branchId);
        List<BranchCommission> oldCommissions = commissionRepository.findByBranch_BranchIdOrderByEffectiveFromDesc(branchId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(c -> c.getIsActive())
                .collect(Collectors.toList());
        
        oldCommissions.forEach(commission -> {
            commission.setIsActive(false);
            log.info("Deactivated old commission: {} for branch: {}", commission.getCommissionId(), branchId);
        });
        
        if (!oldCommissions.isEmpty()) {
            commissionRepository.saveAll(oldCommissions);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCommissionsCount() {
        return commissionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveCommissionsCount() {
        return commissionRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long getExpiringCommissionsCount() {
        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);
        return commissionRepository.findCommissionsExpiringSoon(today, next30Days).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getExpiredCommissionsCount() {
        LocalDate today = LocalDate.now();
        return commissionRepository.findExpiredCommissions(today).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getActiveCommissions() {
        log.info("Fetching active commissions");
        List<BranchCommission> commissions = commissionRepository.findByIsActiveTrueOrderByBranch_NameAsc();
        return commissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getCommissionsByBranch(String branchId) {
        log.info("Fetching commissions for branch: {}", branchId);
        Page<BranchCommission> commissions = commissionRepository.findByBranch_BranchIdOrderByEffectiveFromDesc(branchId, Pageable.unpaged());
        return commissions.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getCommissionsByType(String commissionType) {
        log.info("Fetching commissions by type: {}", commissionType);
        BranchCommission.CommissionType type = BranchCommission.CommissionType.valueOf(commissionType.toUpperCase());
        List<BranchCommission> commissions = commissionRepository.findByCommissionTypeAndIsActiveTrueOrderByBranch_NameAsc(type);
        return commissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getExpiringCommissions() {
        log.info("Fetching expiring commissions");
        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);
        List<BranchCommission> commissions = commissionRepository.findCommissionsExpiringSoon(today, next30Days);
        return commissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchCommissionDTO> getExpiredCommissions() {
        log.info("Fetching expired commissions");
        LocalDate today = LocalDate.now();
        List<BranchCommission> commissions = commissionRepository.findExpiredCommissions(today);
        return commissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCommissionForBranch(String branchId, BigDecimal orderTotal, LocalDate orderDate) {
        log.info("Calculating commission for branch: {} with order total: {} on date: {}", branchId, orderTotal, orderDate);
        
        BranchCommission commission = commissionRepository.findActiveCommissionByBranchAndDate(branchId, orderDate)
                .orElse(null);
        
        if (commission == null) {
            log.warn("No active commission found for branch: {} on date: {}", branchId, orderDate);
            return BigDecimal.ZERO;
        }
        
        BigDecimal calculatedCommission;
        if (commission.getCommissionType() == BranchCommission.CommissionType.PERCENT) {
            calculatedCommission = orderTotal.multiply(commission.getCommissionValue()).divide(new BigDecimal("100"));
        } else {
            calculatedCommission = commission.getCommissionValue();
        }
        
        log.info("Calculated commission: {} for branch: {}", calculatedCommission, branchId);
        return calculatedCommission;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCommissionSummary() {
        log.info("Generating commission summary");
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalCommissions", getTotalCommissionsCount());
        summary.put("activeCommissions", getActiveCommissionsCount());
        summary.put("expiringCommissions", getExpiringCommissionsCount());
        summary.put("expiredCommissions", getExpiredCommissionsCount());
        
        // Add commission type breakdown
        long percentCommissions = commissionRepository.findByCommissionTypeAndIsActiveTrueOrderByBranch_NameAsc(BranchCommission.CommissionType.PERCENT).size();
        long fixedCommissions = commissionRepository.findByCommissionTypeAndIsActiveTrueOrderByBranch_NameAsc(BranchCommission.CommissionType.FIXED).size();
        
        summary.put("percentCommissions", percentCommissions);
        summary.put("fixedCommissions", fixedCommissions);
        
        return summary;
    }

    @Override
    public boolean validateCommission(BranchCommissionDTO dto) {
        if (dto == null) return false;
        if (dto.getBranchId() == null || dto.getBranchId().trim().isEmpty()) return false;
        if (dto.getCommissionType() == null) return false;
        if (dto.getCommissionValue() == null || dto.getCommissionValue().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (dto.getEffectiveFrom() == null) return false;
        if (dto.getEffectiveTo() != null && dto.getEffectiveTo().isBefore(dto.getEffectiveFrom())) return false;
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCommissionOverlapping(String branchId, LocalDate from, LocalDate to, Integer excludeId) {
        List<BranchCommission> existingCommissions = commissionRepository.findByBranch_BranchIdOrderByEffectiveFromDesc(branchId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(c -> c.getIsActive() && !c.getCommissionId().equals(excludeId))
                .collect(Collectors.toList());
        
        for (BranchCommission existing : existingCommissions) {
            LocalDate existingFrom = existing.getEffectiveFrom();
            LocalDate existingTo = existing.getEffectiveTo() != null ? existing.getEffectiveTo() : LocalDate.MAX;
            
            // Check for overlap
            if (from.isBefore(existingTo) && (to == null || to.isAfter(existingFrom))) {
                return true;
            }
        }
        
        return false;
    }

    private BranchCommissionDTO convertToDTO(BranchCommission commission) {
        return BranchCommissionDTO.builder()
                .commissionId(commission.getCommissionId())
                .branchId(commission.getBranch().getBranchId())
                .branchName(commission.getBranch().getName())
                .commissionType(commission.getCommissionType())
                .commissionValue(commission.getCommissionValue())
                .effectiveFrom(commission.getEffectiveFrom())
                .effectiveTo(commission.getEffectiveTo())
                .note(commission.getNote())
                .isActive(commission.getIsActive())
                .createdAt(commission.getCreatedAt())
                .updatedAt(commission.getUpdatedAt())
                .build();
    }
}
