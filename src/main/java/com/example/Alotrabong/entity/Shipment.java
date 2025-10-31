package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipment_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "NVARCHAR(36)")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", columnDefinition = "NVARCHAR(36)")
    private Shipper shipper;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime;

    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status; // 0:Assigned, 1:Đang giao, 2:Đã giao, 3:Hủy

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;
}
