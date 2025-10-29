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
public class ShipperStatsDTO {
    private String shipperId;
    private Long totalDeliveries;
    private Long successfulDeliveries;
    private Long currentDeliveries;
    private Long cancelledDeliveries;
    private BigDecimal successRate;
    private BigDecimal totalDistance;
    private BigDecimal averageDeliveryTime; // in minutes
}
