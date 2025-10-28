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
public class BranchDashboardDTO {
    private String branchId;
    private String branchName;
    
    // Thống kê đơn hàng
    private Long totalOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    
    // Thống kê doanh thu
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal avgOrderValue;
    
    // Thống kê shipper
    private Long totalShippers;
    private Long activeShippers;
    private Long busyShippers;
    
    // Cảnh báo tồn kho
    private Long lowStockItems;
    private List<BranchMenuItemDTO> lowStockItemsList;
    
    // Đơn hàng gần đây
    private List<OrderDTO> recentOrders;
    
    // Khuyến mãi đang hoạt động
    private List<PromotionDTO> activePromotions;
}
