package com.example.Alotrabong.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Branch ID is required")
    private String branchId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}
