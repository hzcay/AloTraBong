package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
                @Index(name = "ix_users_email", columnList = "email", unique = true),
                @Index(name = "ix_users_phone", columnList = "phone", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "user_id", length = 36, columnDefinition = "NVARCHAR(36)")
        private String userId;

        @Column(name = "full_name", length = 120, columnDefinition = "NVARCHAR(120)")
        private String fullName;

        @Column(name = "email", length = 190, unique = true, columnDefinition = "NVARCHAR(190)")
        private String email;

        @Column(name = "phone", length = 20, unique = true, columnDefinition = "NVARCHAR(20)")
        private String phone;

        @Column(name = "password_hash", length = 255, columnDefinition = "NVARCHAR(255)")
        private String password;

        @Column(name = "address", length = 500, columnDefinition = "NVARCHAR(500)")
        private String address;

        // 0: khóa, 1: hoạt động
        @Column(name = "is_active")
        private Boolean isActive;

        @Column(name = "last_login_at")
        private LocalDateTime lastLoginAt;

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        @Builder.Default
        private List<UserRole> userRoles = new ArrayList<>();
}