package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String notificationId;

    @Column(name = "user_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String userId;

    @Column(name = "title", length = 120, columnDefinition = "NVARCHAR(120)")
    private String title;

    @Column(name = "body", length = 300, columnDefinition = "NVARCHAR(300)")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private NotificationType type;

    @Column(name = "is_read")
    private Boolean isRead;
}
