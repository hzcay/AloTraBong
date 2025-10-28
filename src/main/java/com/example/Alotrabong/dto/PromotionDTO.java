package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {
    private String promotionId;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private String branchId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
}
