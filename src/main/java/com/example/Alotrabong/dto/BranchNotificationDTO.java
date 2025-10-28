package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchNotificationDTO {
    private String notificationId;
    private String title;
    private String message;
    private String type; // ORDER, INVENTORY, SHIPPER, PROMOTION
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;
}
