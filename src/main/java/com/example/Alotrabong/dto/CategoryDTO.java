package com.example.Alotrabong.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private String categoryId;
    private String name;
    private String description;
    private Boolean isActive;
    private String parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}