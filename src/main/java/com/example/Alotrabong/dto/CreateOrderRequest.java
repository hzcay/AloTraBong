package com.example.Alotrabong.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull(message = "Branch ID is required")
    private String branchId;
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private String notes;
}