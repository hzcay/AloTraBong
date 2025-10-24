package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDTO {
    private String reportId;
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemName;
    private Integer ordersCount;
    private Integer itemsSold;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String period;
}
