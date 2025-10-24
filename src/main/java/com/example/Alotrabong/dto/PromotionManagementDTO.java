package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionManagementDTO {
    private String promotionId;
    private String code;
    private String description;
    private String type;  // PERCENT, AMOUNT, COMBO, FREESHIP
    private BigDecimal value;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer usageCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

