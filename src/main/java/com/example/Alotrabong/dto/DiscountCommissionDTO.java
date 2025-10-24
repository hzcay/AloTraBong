package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCommissionDTO {
    private String discountId;
    private String branchId;
    private String branchName;
    private BigDecimal commissionRate;
    private BigDecimal discountRate;
    private String description;
    private Boolean isActive;
}

