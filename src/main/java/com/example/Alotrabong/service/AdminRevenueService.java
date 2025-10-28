package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.RevenueReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminRevenueService {
    
    // Revenue by branch
    Page<RevenueReportDTO> getRevenueByBranch(Pageable pageable, LocalDate fromDate, LocalDate toDate, String branchId);
    
    // Revenue by date (daily/monthly)
    List<RevenueReportDTO> getRevenueByDate(LocalDate fromDate, LocalDate toDate, String branchId, String period);
    
    // Revenue by item (top selling items)
    Page<RevenueReportDTO> getRevenueByItem(Pageable pageable, LocalDate fromDate, LocalDate toDate, String branchId);
    
    // Summary statistics
    Map<String, Object> getRevenueSummary(LocalDate fromDate, LocalDate toDate, String branchId);
    
    // Payment method breakdown
    Map<String, Object> getPaymentMethodBreakdown(LocalDate fromDate, LocalDate toDate, String branchId);
    
    // Cancellation statistics
    Map<String, Object> getCancellationStats(LocalDate fromDate, LocalDate toDate, String branchId);
}
