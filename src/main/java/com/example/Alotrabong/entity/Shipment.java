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
    @Column(name = "shipment_id", length = 36)
    private String shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShipmentStatus status; // 0..3

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;
}
