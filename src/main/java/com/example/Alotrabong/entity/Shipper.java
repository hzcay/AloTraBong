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
    @Column(name = "shipper_id", length = 36)
    private String shipperId;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Column(name = "is_active")
    private Boolean isActive;
}
