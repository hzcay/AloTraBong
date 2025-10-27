package com.example.Alotrabong.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMediaDTO {
    private Integer mediaId;
    private String itemId;
    private String mediaUrl;
    private String mediaType;
    private Integer sortOrder;
}

