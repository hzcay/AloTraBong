package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", columnDefinition = "NVARCHAR(36)")
    private Shipment shipment;

    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status; // Mốc trạng thái

    @Column(name = "note", length = 200, columnDefinition = "NVARCHAR(200)")
    private String note;

    @Column(name = "event_time")
    private LocalDateTime eventTime;
}
