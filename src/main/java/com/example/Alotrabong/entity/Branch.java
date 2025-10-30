package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "branches", uniqueConstraints = @UniqueConstraint(name = "uk_branch_code", columnNames = {
        "branch_code" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id", length = 36)
    private String branchId;

    @Column(name = "branch_code", length = 30)
    private String branchCode;

    @Column(name = "branch_name", length = 120)
    private String name;

    @Column(name = "address_line", length = 255, columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "phone", length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "district", length = 100, columnDefinition = "NVARCHAR(100)")
    private String district;

    @Column(name = "city", length = 100, columnDefinition = "NVARCHAR(100)")
    private String city;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "open_hours", length = 120, columnDefinition = "NVARCHAR(120)")
    private String openHours;
}