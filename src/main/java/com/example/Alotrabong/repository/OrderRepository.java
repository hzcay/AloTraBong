package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByBranchOrderByCreatedAtDesc(Branch branch);
    
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    
    List<Order> findByBranchAndStatusAndCreatedAtBetween(
            Branch branch, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
