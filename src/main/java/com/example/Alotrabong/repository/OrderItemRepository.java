package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderItem;
import com.example.Alotrabong.entity.OrderStatus;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.domain.Pageable;  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrder(Order order);

    @Query("""
            SELECT oi.item.itemId, SUM(oi.quantity)
            FROM OrderItem oi
            WHERE oi.order.status IN :okStatuses
              AND oi.order.createdAt >= :since
            GROUP BY oi.item.itemId
            ORDER BY SUM(oi.quantity) DESC
          """)
    List<Object[]> findTopSellingSince(
            @Param("since") LocalDateTime since,
            @Param("okStatuses") List<OrderStatus> okStatuses,
            Pageable pageable
    );
}
