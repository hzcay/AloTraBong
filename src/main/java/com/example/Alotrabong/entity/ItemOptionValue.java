package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_option_values")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemOptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Integer valueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ItemOption option;

    @Column(name = "value_name", length = 80)
    private String valueName;

    @Column(name = "extra_price", precision = 12, scale = 2)
    private BigDecimal extraPrice;
}
