package com.example.Alotrabong.dto;

import com.example.Alotrabong.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderHistoryVM(
                String code, // #<span th:text="${o.code}">
                LocalDateTime createdAt, // o.createdAt
                String branchName, // o.branchName
                OrderStatus status, // o.status
                BigDecimal grandTotal, // o.grandTotal
                List<ItemVM> items // o.items (rút gọn)
) {
        public record ItemVM(
                        String itemId,
                        String name,
                        Integer qty,
                        BigDecimal unitPrice,
                        BigDecimal subtotal,
                        String thumbnailUrl) {
        }
}
