package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // ===== Lọc nhanh theo user/branch/status (sort mới nhất) =====
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByBranchOrderByCreatedAtDesc(Branch branch);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByBranchAndStatusAndCreatedAtBetween(
            Branch branch,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // ===== Phân trang theo user (tránh N+1 khi cần items + item) =====
    @EntityGraph(attributePaths = { "items", "items.item" })
    Page<Order> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = { "items", "items.item" })
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    // ===== Phân trang + lọc theo khoảng thời gian =====
    @EntityGraph(attributePaths = { "items", "items.item" })
    Page<Order> findByUserAndCreatedAtBetween(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @EntityGraph(attributePaths = { "items", "items.item" })
    Page<Order> findByUserAndStatusAndCreatedAtBetween(
            User user,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // ===== Doanh thu nhanh gọn (sum) =====
    @Query("""
           select coalesce(sum(o.totalAmount), 0)
           from Order o
           where o.branch = :branch
             and o.createdAt >= :start
             and o.createdAt < :end
             and o.status in (
                com.example.Alotrabong.entity.OrderStatus.DELIVERED,
                com.example.Alotrabong.entity.OrderStatus.REFUNDED
             )
           """)
    BigDecimal sumRevenueByBranchAndDate(
            Branch branch,
            LocalDateTime start,
            LocalDateTime end
    );

    // ===== Tìm theo code/ID =====
    Order findFirstByOrderId(String orderId);
    
}
