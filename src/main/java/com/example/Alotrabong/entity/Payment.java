package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = @Index(name = "ix_payments_order", columnList = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String paymentId;

    @Column(name = "order_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private PaymentMethod provider; // VNPAY/MOMO/COD

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "txn_code", length = 80, columnDefinition = "NVARCHAR(80)")
    private String txnCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status; // INIT/SUCCESS/FAIL -> mapped to enum PAID/...

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
