package com.example.Alotrabong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Comparator;

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

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<ItemMedia> mediaList;

    @Transient
    public String getThumbnailUrl() {
        if (mediaList == null || mediaList.isEmpty())
            return "/img/placeholder.jpg";
        return mediaList.stream()
                .filter(m -> m.getMediaType() == MediaType.IMAGE)
                .sorted(Comparator
                        .comparing((ItemMedia m) -> m.getSortOrder() == null ? 9999 : m.getSortOrder())
                        .thenComparing(ItemMedia::getMediaId))
                .map(ItemMedia::getMediaUrl)
                .findFirst()
                .orElse("/img/placeholder.jpg");
    }
}
