package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_branches", uniqueConstraints = @UniqueConstraint(name = "uk_promo_branch", columnNames = {
        "promo_id", "branch_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_branch_id")
    private Integer promoBranchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
