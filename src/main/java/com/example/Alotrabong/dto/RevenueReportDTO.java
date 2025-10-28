package com.example.Alotrabong.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueReportDTO {
    // Common fields
    private String branchId;
    private String branchName;
    private LocalDate reportDate;
    private LocalDateTime reportDateTime;
    
    // Order metrics
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalShippingFee;
    private BigDecimal avgOrderValue;
    
    // Commission calculations
    private BigDecimal systemShare;
    private BigDecimal branchIncome;
    private BigDecimal commissionRate;
    private String commissionType; // PERCENT or FIXED
    
    // Payment method breakdown
    private Long onlineOrders;
    private Long codOrders;
    private BigDecimal onlineRevenue;
    private BigDecimal codRevenue;
    
    // Cancellation metrics
    private Long cancelledOrders;
    private Long refundedOrders;
    private BigDecimal cancellationRate;
    
    // Top items (for item-based reports)
    private String itemId;
    private String itemName;
    private Long itemQuantity;
    private BigDecimal itemRevenue;
}