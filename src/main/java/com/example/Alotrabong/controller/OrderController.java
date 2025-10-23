package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.CreateOrderRequest;
import com.example.Alotrabong.dto.OrderDTO;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create new order")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        List<OrderDTO> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User orders retrieved", orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable String orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason,
            Authentication authentication) {
        OrderDTO order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", order));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get orders by branch")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByBranch(@PathVariable String branchId) {
        List<OrderDTO> orders = orderService.getOrdersByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch orders retrieved", orders));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Orders by status retrieved", orders));
    }

    @GetMapping("/revenue/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get total revenue for branch")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue(
            @PathVariable String branchId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        BigDecimal revenue = orderService.getTotalRevenue(branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Total revenue calculated", revenue));
    }

    private String getUserIdFromAuth(Authentication authentication) {
        // TODO: Implement based on your JWT authentication setup
        return "user-id-placeholder"; // Placeholder
    }
}
