package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Column(name = "action", length = 80, columnDefinition = "NVARCHAR(80)")
    private String action;

    @Column(name = "entity", length = 80, columnDefinition = "NVARCHAR(80)")
    private String entity;

    @Column(name = "entity_id", length = 80, columnDefinition = "NVARCHAR(80)")
    private String entityId;

    @Column(name = "meta_json", columnDefinition = "nvarchar(max)")
    private String metaJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
