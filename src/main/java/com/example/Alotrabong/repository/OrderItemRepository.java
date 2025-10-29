package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderItem;
import com.example.Alotrabong.entity.OrderStatus;

import org.springframework.data.repository.query.Param;

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
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId IN :orderIds")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<String> orderIds);
    
    @Query("""
            SELECT oi.item.itemId, oi.item.name, SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice)
            FROM OrderItem oi
            WHERE oi.order.branch.branchId = :branchId
              AND oi.order.status = :status
              AND COALESCE(oi.order.updatedAt, oi.order.createdAt) >= :startDate
              AND COALESCE(oi.order.updatedAt, oi.order.createdAt) < :endDate
              AND (oi.order.paymentStatus IS NULL OR oi.order.paymentStatus != com.example.Alotrabong.entity.PaymentStatus.REFUNDED)
            GROUP BY oi.item.itemId, oi.item.name
            ORDER BY SUM(oi.quantity) DESC
          """)
    List<Object[]> findTopSellingItemsByBranchAndDateRange(
            @Param("branchId") String branchId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
