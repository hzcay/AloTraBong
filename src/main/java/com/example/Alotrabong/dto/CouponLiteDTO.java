package com.example.Alotrabong.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponLiteDTO(
        String code,           // SALE10, ALO20...
        String type,           // "PERCENT" | "AMOUNT"
        BigDecimal value,      // 10 (percent) | 20000 (amount)
        BigDecimal minSubtotal,
        BigDecimal maxDiscount,
        LocalDateTime endsAt
) {}
