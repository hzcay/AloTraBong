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
public class TopSellingItemDTO {
    private String itemId;
    private String itemName;
    private String itemCode;
    private Long totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal avgPrice;
    private Integer rank;
}
