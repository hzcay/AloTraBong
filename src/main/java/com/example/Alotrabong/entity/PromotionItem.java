package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_items",
       uniqueConstraints = @UniqueConstraint(name = "uk_promo_item", columnNames = {"promo_id", "item_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_item_id")
    private Integer promoItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}
