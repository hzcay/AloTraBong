package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items", indexes = @Index(name = "ix_items_code", columnList = "item_code", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", length = 36)
    private String itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "item_code", length = 40)
    private String itemCode;

    @Column(name = "item_name", length = 160)
    private String name;

    @Column(name = "description", length = 800)
    private String description;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "is_active")
    private Boolean isActive;
}
