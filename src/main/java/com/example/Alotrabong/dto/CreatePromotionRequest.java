package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromotionRequest {
    private String name;
    private String description;
    private String discountType; // PERCENT, AMOUNT, FREESHIP, COMBO
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minOrderAmount;
    private Integer maxUsage;
    private String[] itemIds; // Món ăn áp dụng khuyến mãi
}
