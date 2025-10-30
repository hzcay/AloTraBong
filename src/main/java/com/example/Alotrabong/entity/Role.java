package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_code", columnNames = { "role_code" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", length = 40, nullable = false)
    private RoleCode roleCode;

    @Column(name = "role_name", length = 80, columnDefinition = "NVARCHAR(80)")
    private String roleName;
}