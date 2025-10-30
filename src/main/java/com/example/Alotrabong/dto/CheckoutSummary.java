package com.example.Alotrabong.dto;

import java.math.BigDecimal;

public record CheckoutSummary(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingFee,
        BigDecimal grandTotal
) {}
