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
    private String userName;
    private String userEmail;
    private String userPhone;
    private String branchId;
    private String branchName;
    private String vehiclePlate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display
    private Integer totalShipments;
    private Integer completedShipments;
    private Integer activeShipments;
    private Double averageRating;
}
