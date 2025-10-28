package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchRevenueReportDTO {
    private String branchId;
    private String branchName;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Thống kê đơn hàng
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long refundedOrders;
    
    // Thống kê doanh thu
    private BigDecimal totalRevenue;
    private BigDecimal completedRevenue;
    private BigDecimal cancelledRevenue;
    private BigDecimal refundedRevenue;
    private BigDecimal avgOrderValue;
    
    // Thống kê theo phương thức thanh toán
    private BigDecimal cashRevenue;
    private BigDecimal cardRevenue;
    private BigDecimal vnpayRevenue;
    
    // Top sản phẩm bán chạy
    private List<TopSellingItemDTO> topSellingItems;
    
    // Doanh thu theo ngày
    private List<DailyRevenueDTO> dailyRevenue;
    
    // Tỷ lệ hoàn thành đơn hàng
    private BigDecimal completionRate;
    private BigDecimal cancellationRate;
}
