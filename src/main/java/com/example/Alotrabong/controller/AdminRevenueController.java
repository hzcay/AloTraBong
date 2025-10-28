package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.RevenueReportDTO;
import com.example.Alotrabong.service.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/revenue")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminRevenueController {

    private final AdminRevenueService adminRevenueService;

    @GetMapping("/by-branch")
    public ResponseEntity<Page<RevenueReportDTO>> getRevenueByBranch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId) {
        
        log.info("Getting revenue by branch - page: {}, size: {}, fromDate: {}, toDate: {}, branchId: {}", 
                page, size, fromDate, toDate, branchId);
        
        Pageable pageable = PageRequest.of(page, size);
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        Page<RevenueReportDTO> reports = adminRevenueService.getRevenueByBranch(pageable, from, to, branchId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<RevenueReportDTO>> getRevenueByDate(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId,
            @RequestParam(defaultValue = "daily") String period) {
        
        log.info("Getting revenue by date - fromDate: {}, toDate: {}, branchId: {}, period: {}", 
                fromDate, toDate, branchId, period);
        
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        List<RevenueReportDTO> reports = adminRevenueService.getRevenueByDate(from, to, branchId, period);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/by-item")
    public ResponseEntity<Page<RevenueReportDTO>> getRevenueByItem(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId) {
        
        log.info("Getting revenue by item - page: {}, size: {}, fromDate: {}, toDate: {}, branchId: {}", 
                page, size, fromDate, toDate, branchId);
        
        Pageable pageable = PageRequest.of(page, size);
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        Page<RevenueReportDTO> reports = adminRevenueService.getRevenueByItem(pageable, from, to, branchId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId) {
        
        log.info("Getting revenue summary - fromDate: {}, toDate: {}, branchId: {}", fromDate, toDate, branchId);
        
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        Map<String, Object> summary = adminRevenueService.getRevenueSummary(from, to, branchId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/payment-breakdown")
    public ResponseEntity<Map<String, Object>> getPaymentMethodBreakdown(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId) {
        
        log.info("Getting payment method breakdown - fromDate: {}, toDate: {}, branchId: {}", fromDate, toDate, branchId);
        
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        Map<String, Object> breakdown = adminRevenueService.getPaymentMethodBreakdown(from, to, branchId);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/cancellation-stats")
    public ResponseEntity<Map<String, Object>> getCancellationStats(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String branchId) {
        
        log.info("Getting cancellation stats - fromDate: {}, toDate: {}, branchId: {}", fromDate, toDate, branchId);
        
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        Map<String, Object> stats = adminRevenueService.getCancellationStats(from, to, branchId);
        return ResponseEntity.ok(stats);
    }
}
