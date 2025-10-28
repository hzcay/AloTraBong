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
public class ShipmentEventDTO {
    private Long eventId;
    private String shipmentId;
    private Integer status;
    private String statusText;
    private String note;
    private LocalDateTime eventTime;
}
