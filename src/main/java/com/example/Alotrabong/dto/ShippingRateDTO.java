package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRateDTO {
    private String rateId;
    private String district;
    private String city;
    private BigDecimal baseRate;
    private BigDecimal perKmRate;
    private BigDecimal maxDistance;
    private Boolean isActive;
}

