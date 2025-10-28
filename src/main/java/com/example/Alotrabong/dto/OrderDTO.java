package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private String orderId;
    private String userId;
    private String branchId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal grandTotal;
    private String deliveryAddress;
    private String deliveryPhone;
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private String shipperName;
    private String shipperPhone;
    private String shippingAddress; // Keep for backward compatibility
    private String notes;
    private List<OrderItemDTO> orderItems;
    private List<OrderItemDTO> items; // Alias for orderItems
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
