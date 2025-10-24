package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.RevenueReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AdminReportService {

    // Revenue & Statistics Reports
    Page<RevenueReportDTO> getRevenueByBranch(Pageable pageable, LocalDate startDate, LocalDate endDate);

    Page<RevenueReportDTO> getRevenueByItem(Pageable pageable, LocalDate startDate, LocalDate endDate);

    RevenueReportDTO getRevenueByBranchAndItem(String branchId, String itemId, LocalDate startDate, LocalDate endDate);

    double getTotalSystemRevenue(LocalDate startDate, LocalDate endDate);

    double getTotalBranchRevenue(String branchId, LocalDate startDate, LocalDate endDate);

    List<RevenueReportDTO> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit);

    List<RevenueReportDTO> getTopBranches(LocalDate startDate, LocalDate endDate, int limit);

    long getTotalOrdersCount(LocalDate startDate, LocalDate endDate);

    double getAverageOrderValue(LocalDate startDate, LocalDate endDate);

    long getTotalItemsSold(LocalDate startDate, LocalDate endDate);
}


