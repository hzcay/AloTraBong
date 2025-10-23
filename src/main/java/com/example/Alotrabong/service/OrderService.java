package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.CreateOrderRequest;
import com.example.Alotrabong.dto.OrderDTO;
import com.example.Alotrabong.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    
    OrderDTO createOrder(String userId, CreateOrderRequest request);
    
    OrderDTO getOrderById(String orderId);
    
    List<OrderDTO> getOrdersByUser(String userId);
    
    List<OrderDTO> getOrdersByBranch(String branchId);
    
    OrderDTO updateOrderStatus(String orderId, OrderStatus status);
    
    OrderDTO cancelOrder(String orderId, String reason);
    
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    
    BigDecimal getTotalRevenue(String branchId, LocalDateTime startDate, LocalDateTime endDate);
}