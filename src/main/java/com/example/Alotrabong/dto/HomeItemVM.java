package com.example.Alotrabong.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeItemVM {
    private String id; // <- dùng cho form hidden (template đang gọi p.id)
    private String name;
    private BigDecimal price; // template đang format số => BigDecimal OK
    private String thumbnailUrl; // tạm lấy placeholder vì chưa có ItemMedia
}
