package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_rates",
       uniqueConstraints = @UniqueConstraint(name = "uk_rate_branch", columnNames = {"branch_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingRate extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Integer rateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "base_fee", precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "per_km_fee", precision = 12, scale = 2)
    private BigDecimal perKmFee;

    @Column(name = "free_ship_threshold", precision = 12, scale = 2)
    private BigDecimal freeShipThreshold;
}
