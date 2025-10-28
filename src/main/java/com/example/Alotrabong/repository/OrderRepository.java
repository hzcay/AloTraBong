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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.query.Param;

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
    
    // ===== Revenue reporting methods =====
    List<Order> findByStatusAndUpdatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    
    List<Order> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // ===== Branch Manager specific methods =====
    
    // Count methods
    Long countByBranch_BranchId(String branchId);
    Long countByBranch_BranchIdAndStatus(String branchId, OrderStatus status);
    
    // Revenue methods
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.branch.branchId = :branchId AND CAST(o.createdAt AS date) = CURRENT_DATE AND o.status = 'DELIVERED'")
    BigDecimal getTodayRevenue(String branchId);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.branch.branchId = :branchId AND YEAR(o.createdAt) = YEAR(CURRENT_DATE) AND MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND o.status = 'DELIVERED'")
    BigDecimal getMonthlyRevenue(String branchId);
    
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.branch.branchId = :branchId AND o.status = 'DELIVERED'")
    BigDecimal getAvgOrderValue(String branchId);
    
    // Recent orders
    List<Order> findTop5ByBranch_BranchIdOrderByCreatedAtDesc(String branchId);
    
    // Date range methods
    @Query("SELECT COUNT(o) FROM Order o WHERE o.branch.branchId = :branchId AND CAST(o.createdAt AS date) BETWEEN :startDate AND :endDate")
    Long countByBranchAndDateRange(@Param("branchId") String branchId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.branch.branchId = :branchId AND o.status = :status AND CAST(o.createdAt AS date) BETWEEN :startDate AND :endDate")
    Long countByBranchAndStatusAndDateRange(@Param("branchId") String branchId, @Param("status") OrderStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.branch.branchId = :branchId AND CAST(o.createdAt AS date) BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    BigDecimal getRevenueByBranchAndDateRange(@Param("branchId") String branchId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.branch.branchId = :branchId AND o.status = :status AND CAST(o.createdAt AS date) BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByBranchAndStatusAndDateRange(@Param("branchId") String branchId, @Param("status") OrderStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Find by ID and branch
    Order findByOrderIdAndBranch_BranchId(String orderId, String branchId);
    
    // Additional methods for BranchManager
    Page<Order> findByBranch(Branch branch, Pageable pageable);
    Page<Order> findByBranchAndStatus(Branch branch, OrderStatus status, Pageable pageable);
}
