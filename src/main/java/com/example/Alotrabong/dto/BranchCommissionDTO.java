package com.example.Alotrabong.dto;

import com.example.Alotrabong.entity.BranchCommission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchCommissionDTO {

    private Integer commissionId;
    private String branchId;
    private String branchName;
    private BranchCommission.CommissionType commissionType;
    private BigDecimal commissionValue;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields for UI
    private String commissionTypeDisplay;
    private String commissionValueDisplay;
    private String statusDisplay;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Long daysUntilExpiry;
    private String effectivePeriodDisplay;

    // Helper methods for display
    public String getCommissionTypeDisplay() {
        if (commissionType == null) return "N/A";
        return commissionType == BranchCommission.CommissionType.PERCENT ? "Phần trăm (%)" : "Số tiền cố định";
    }

    public String getCommissionValueDisplay() {
        if (commissionValue == null) return "N/A";
        if (commissionType == BranchCommission.CommissionType.PERCENT) {
            return commissionValue + "%";
        } else {
            return String.format("%,.0f ₫", commissionValue.doubleValue());
        }
    }

    public String getStatusDisplay() {
        if (isActive == null || !isActive) return "Không hoạt động";
        
        LocalDate today = LocalDate.now();
        if (effectiveTo != null && effectiveTo.isBefore(today)) {
            return "Đã hết hạn";
        }
        if (effectiveFrom.isAfter(today)) {
            return "Chưa có hiệu lực";
        }
        return "Đang hoạt động";
    }

    public Boolean getIsExpired() {
        if (effectiveTo == null) return false;
        return effectiveTo.isBefore(LocalDate.now());
    }

    public Boolean getIsExpiringSoon() {
        if (effectiveTo == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);
        return effectiveTo.isAfter(today) && effectiveTo.isBefore(next30Days);
    }

    public Long getDaysUntilExpiry() {
        if (effectiveTo == null) return null;
        LocalDate today = LocalDate.now();
        if (effectiveTo.isBefore(today)) return 0L;
        return java.time.temporal.ChronoUnit.DAYS.between(today, effectiveTo);
    }

    public String getEffectivePeriodDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Từ ").append(effectiveFrom);
        if (effectiveTo != null) {
            sb.append(" đến ").append(effectiveTo);
        } else {
            sb.append(" (không giới hạn)");
        }
        return sb.toString();
    }
}
