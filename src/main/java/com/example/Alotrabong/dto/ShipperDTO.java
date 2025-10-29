package com.example.Alotrabong.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperDTO {
    private String shipperId;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String branchId;
    private String branchName;
    private String vehiclePlate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display
    private Integer totalDeliveries;
    private Integer currentDeliveries;
    private Integer successfulDeliveries;
    private Double successRate;
}
