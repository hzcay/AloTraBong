package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "option_name", length = 80)
    private String optionName;

    @Column(name = "is_required")
    private Boolean isRequired;
}
