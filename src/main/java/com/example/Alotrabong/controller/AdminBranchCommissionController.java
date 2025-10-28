package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.BranchCommissionDTO;
import com.example.Alotrabong.service.AdminBranchCommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/branch-commissions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminBranchCommissionController {

    private final AdminBranchCommissionService commissionService;

    @GetMapping
    public ResponseEntity<Page<BranchCommissionDTO>> getAllCommissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.info("Fetching all commissions - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BranchCommissionDTO> commissions = commissionService.getAllCommissions(pageable, search);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BranchCommissionDTO>> getAllCommissions() {
        log.info("Fetching all commissions");
        List<BranchCommissionDTO> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/{commissionId}")
    public ResponseEntity<BranchCommissionDTO> getCommissionById(@PathVariable Integer commissionId) {
        log.info("Fetching commission by id: {}", commissionId);
        BranchCommissionDTO commission = commissionService.getCommissionById(commissionId);
        return ResponseEntity.ok(commission);
    }

    @GetMapping("/branch/{branchId}/active")
    public ResponseEntity<BranchCommissionDTO> getActiveCommissionByBranch(@PathVariable String branchId) {
        log.info("Fetching active commission for branch: {}", branchId);
        BranchCommissionDTO commission = commissionService.getActiveCommissionByBranchId(branchId);
        return ResponseEntity.ok(commission);
    }

    @PostMapping
    public ResponseEntity<BranchCommissionDTO> createCommission(@RequestBody BranchCommissionDTO dto) {
        log.info("Creating new commission for branch: {}", dto.getBranchId());
        BranchCommissionDTO commission = commissionService.createCommission(dto);
        return ResponseEntity.ok(commission);
    }

    @PutMapping("/{commissionId}")
    public ResponseEntity<BranchCommissionDTO> updateCommission(
            @PathVariable Integer commissionId,
            @RequestBody BranchCommissionDTO dto) {
        log.info("Updating commission: {}", commissionId);
        BranchCommissionDTO commission = commissionService.updateCommission(commissionId, dto);
        return ResponseEntity.ok(commission);
    }

    @DeleteMapping("/{commissionId}")
    public ResponseEntity<Map<String, String>> deleteCommission(@PathVariable Integer commissionId) {
        log.info("Deleting commission: {}", commissionId);
        commissionService.deleteCommission(commissionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Commission deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{commissionId}/activate")
    public ResponseEntity<Map<String, String>> activateCommission(@PathVariable Integer commissionId) {
        log.info("Activating commission: {}", commissionId);
        commissionService.activateCommission(commissionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Commission activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{commissionId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateCommission(@PathVariable Integer commissionId) {
        log.info("Deactivating commission: {}", commissionId);
        commissionService.deactivateCommission(commissionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Commission deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<BranchCommissionDTO>> getCommissionsByBranch(@PathVariable String branchId) {
        log.info("Fetching commissions for branch: {}", branchId);
        List<BranchCommissionDTO> commissions = commissionService.getCommissionsByBranch(branchId);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/type/{commissionType}")
    public ResponseEntity<List<BranchCommissionDTO>> getCommissionsByType(@PathVariable String commissionType) {
        log.info("Fetching commissions by type: {}", commissionType);
        List<BranchCommissionDTO> commissions = commissionService.getCommissionsByType(commissionType);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<BranchCommissionDTO>> getExpiringCommissions() {
        log.info("Fetching expiring commissions");
        List<BranchCommissionDTO> commissions = commissionService.getExpiringCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<BranchCommissionDTO>> getExpiredCommissions() {
        log.info("Fetching expired commissions");
        List<BranchCommissionDTO> commissions = commissionService.getExpiredCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BranchCommissionDTO>> getActiveCommissions() {
        log.info("Fetching active commissions");
        List<BranchCommissionDTO> commissions = commissionService.getActiveCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getCommissionStats() {
        log.info("Fetching commission statistics");
        Map<String, Object> stats = commissionService.getCommissionSummary();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/calculate/{branchId}")
    public ResponseEntity<Map<String, Object>> calculateCommission(
            @PathVariable String branchId,
            @RequestParam String orderTotal,
            @RequestParam(required = false) String orderDate) {
        log.info("Calculating commission for branch: {} with order total: {}", branchId, orderTotal);
        
        try {
            java.math.BigDecimal total = new java.math.BigDecimal(orderTotal);
            java.time.LocalDate date = orderDate != null ? 
                java.time.LocalDate.parse(orderDate) : 
                java.time.LocalDate.now();
            
            java.math.BigDecimal commission = commissionService.calculateCommissionForBranch(branchId, total, date);
            
            Map<String, Object> result = new HashMap<>();
            result.put("branchId", branchId);
            result.put("orderTotal", total);
            result.put("orderDate", date);
            result.put("calculatedCommission", commission);
            result.put("commissionRate", commission.divide(total, 4, java.math.RoundingMode.HALF_UP).multiply(new java.math.BigDecimal("100")));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error calculating commission: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid input parameters");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
