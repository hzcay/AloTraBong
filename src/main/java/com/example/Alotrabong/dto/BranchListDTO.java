package com.example.Alotrabong.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchListDTO {
    private String branchId;
    private String name;
    private String address;
    private String phone;
    private Boolean isActive;
    private String openHours;

    // NEW: to draw markers
    private Double latitude;   // nullable
    private Double longitude;  // nullable

    // chỉ dùng cho view
    private Double distanceKm; // nullable
    private Integer deliveryEtaMin; // nullable
    private Boolean isDefault; // nullable/false
}
