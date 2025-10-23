package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(name = "uk_inv_branch_item", columnNames = {"branch_id", "item_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inv_id")
    private Integer invId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "safety_stock")
    private Integer safetyStock;
}
