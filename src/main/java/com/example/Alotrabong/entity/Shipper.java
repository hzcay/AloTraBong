package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shippers", uniqueConstraints = @UniqueConstraint(name = "uk_shipper_user", columnNames = { "user_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipper extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipper_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String shipperId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "vehicle_plate", length = 20, columnDefinition = "NVARCHAR(20)")
    private String vehiclePlate;

    @Column(name = "is_active")
    private Boolean isActive;
}
