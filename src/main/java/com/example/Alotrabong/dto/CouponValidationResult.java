package com.example.Alotrabong.dto;

import java.math.BigDecimal;

public record CouponValidationResult(
        boolean ok,
        BigDecimal previewDiscount,
        String message
) {
    public static CouponValidationResult ok(BigDecimal d, String msg) {
        return new CouponValidationResult(true, d, msg);
    }
    public static CouponValidationResult fail(String msg) {
        return new CouponValidationResult(false, BigDecimal.ZERO, msg);
    }
}
