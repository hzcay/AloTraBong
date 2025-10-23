package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserOtp extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20)
    private OtpPurpose purpose;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_used")
    private Boolean isUsed;
}
