package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_item_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_item_opt_id", length = 36)
    private String cartItemOptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id")
    private CartItem cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ItemOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id")
    private ItemOptionValue value;

    @Column(name = "extra_price", precision = 12, scale = 2)
    private BigDecimal extraPrice;
}
