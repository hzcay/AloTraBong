package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchMenuItemDTO {
    private String itemId;
    private String itemName;
    private String itemCode;
    private String description;
    private String categoryName;
    
    // Giá và trạng thái bán
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private Boolean isAvailable;
    
    // Tồn kho
    private Integer quantity;
    private Integer safetyStock;
    private Boolean isLowStock;
    
    // Thống kê bán hàng
    private Long totalSold;
    private BigDecimal totalRevenue;
}
