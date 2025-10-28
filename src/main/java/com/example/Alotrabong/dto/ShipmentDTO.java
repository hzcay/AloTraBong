package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO {
    private String shipmentId;
    private String orderId;
    private String shipperId;
    private String shipperName;
    private String shipperPhone;
    private Integer status; // 0:Assigned, 1:Đang giao, 2:Đã giao, 3:Hủy
    private String statusText;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveredTime;
    private BigDecimal distanceKm;
    private String note;
    private List<ShipmentEventDTO> events;
}
