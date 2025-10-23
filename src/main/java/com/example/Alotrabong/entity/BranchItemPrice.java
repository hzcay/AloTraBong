package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "branch_item_prices",
       uniqueConstraints = @UniqueConstraint(name = "uk_branch_item", columnNames = {"branch_id", "item_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchItemPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_item_id")
    private Integer branchItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available")
    private Boolean isAvailable;
}
