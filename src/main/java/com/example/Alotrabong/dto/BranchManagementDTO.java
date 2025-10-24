package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchManagementDTO {
    private String branchId;
    private String branchCode;
    private String name;
    private String address;
    private String phone;
    private String district;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
    private String openHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemCount;
    private Double totalRevenue;
}

