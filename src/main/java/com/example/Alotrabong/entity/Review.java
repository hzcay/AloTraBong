package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_order_item", columnNames = {
        "order_id", "item_id", "user_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String reviewId;

    @Column(name = "item_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String itemId;

    @Column(name = "user_id", length = 36, columnDefinition = "NVARCHAR(36)")
    private String userId;

    @Column(name = "rating")
    private Integer rating; // 1–5

    @Column(name = "comment", length = 800, columnDefinition = "NVARCHAR(800)")
    private String comment;

    @Column(name = "is_active")
    private Boolean isActive;
}
