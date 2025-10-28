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
public class AddItemToBranchRequest {
    private String itemId;
    private BigDecimal salePrice;
    private Boolean isAvailable;
    private Integer initialQuantity;
    private Integer safetyStock;
}
