package com.example.Alotrabong.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOptionDTO {
    private Integer optionId;
    private String itemId;
    private String optionName;
    private Boolean isRequired;
    private List<ItemOptionValueDTO> values;
}

