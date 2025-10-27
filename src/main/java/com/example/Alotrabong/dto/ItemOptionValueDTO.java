package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOptionValueDTO {
    private Integer valueId;
    private Integer optionId;
    private String valueName;
    private BigDecimal extraPrice;
}

