package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDetailDTO {
    private String itemId;
    private String itemCode;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer calories;
    private Boolean isActive;
    private String categoryId;
    private String categoryName;
    private List<ItemMediaDTO> media;
    private List<ItemOptionDTO> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
