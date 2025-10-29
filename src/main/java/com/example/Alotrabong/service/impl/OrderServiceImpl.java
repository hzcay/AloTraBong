package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.CreateOrderRequest;
import com.example.Alotrabong.dto.OrderDTO;
import com.example.Alotrabong.dto.OrderItemDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        // private final PaymentRepository paymentRepository; // TODO: Implement payment
        // logic

        @Override
        public OrderDTO createOrder(String userId, CreateOrderRequest request) {
                log.info("Creating order for user: {}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Branch branch = branchRepository.findById(request.getBranchId())
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

                // Get cart items
                Cart cart = cartRepository.findByUserAndBranch(user, branch)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

                List<CartItem> cartItems = cartItemRepository.findByCart(cart);
                if (cartItems.isEmpty()) {
                        throw new BadRequestException("Cart is empty");
                }

                // 1. Subtotal (tiền món)
                BigDecimal subtotal = cartItems.stream()
                                .map(item -> item.getUnitPrice()
                                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 2. Shipping fee (phí giao)
                // TODO: thay bằng logic real của bạn (ví dụ theo branch, theo distance,...)
                BigDecimal shippingFee = new BigDecimal("15000"); // tạm hardcode như UI bạn show

                // 3. Discount từ coupon (nếu có)
                // Hiện CreateOrderRequest không có couponCode, và service này chưa đọc session
                // coupon,
                // nên mình set = 0 cho an toàn để không làm app crash.
                BigDecimal discount = BigDecimal.ZERO;

                // 4. Grand total = subtotal - discount + shippingFee
                BigDecimal grandTotal = subtotal
                                .subtract(discount)
                                .add(shippingFee);

                // 5. Build Order
                Order order = Order.builder()
                                .user(user)
                                .branch(branch)
                                .status(OrderStatus.PENDING)
                                .paymentMethod(PaymentMethod.COD)
                                .totalAmount(grandTotal) // <- LÚC NÀY LÀ 75k + 15k = 90k
                                .shippingAddress(request.getShippingAddress())
                                .notes(request.getNotes())
                                .build();

                order = orderRepository.saveAndFlush(order);

                // 6. Save từng dòng chi tiết
                for (CartItem cartItem : cartItems) {
                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .item(cartItem.getItem())
                                        .quantity(cartItem.getQuantity())
                                        .unitPrice(cartItem.getUnitPrice())
                                        .build();
                        orderItemRepository.save(orderItem);
                }

                // 7. Clear cart
                cartItemRepository.deleteByCart(cart);

                log.info("Order created successfully: {}", order.getOrderId());
                return convertToDTO(order);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderDTO getOrderById(String orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                return convertToDTO(order);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderDTO> getOrdersByUser(String userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderDTO> getOrdersByBranch(String branchId) {
                Branch branch = branchRepository.findById(branchId)
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

                return orderRepository.findByBranchOrderByCreatedAtDesc(branch).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public OrderDTO updateOrderStatus(String orderId, OrderStatus status) {
                log.info("Updating order status: {} to {}", orderId, status);

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                order.setStatus(status);
                order = orderRepository.save(order);

                // TODO: Create status history entry
                // OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                // .order(order)
                // .status(status)
                // .notes("Status updated to " + status)
                // .build();
                // TODO: Save status history

                log.info("Order status updated: {} to {}", orderId, status);
                return convertToDTO(order);
        }

        @Override
        public OrderDTO cancelOrder(String orderId, String reason) {
                log.info("Cancelling order: {} with reason: {}", orderId, reason);

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
                        throw new BadRequestException("Cannot cancel order in current status");
                }

                order.setStatus(OrderStatus.CANCELLED);
                order = orderRepository.save(order);

                log.info("Order cancelled: {}", orderId);
                return convertToDTO(order);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
                return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public BigDecimal getTotalRevenue(String branchId, LocalDateTime startDate, LocalDateTime endDate) {
                Branch branch = branchRepository.findById(branchId)
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

                List<Order> orders = orderRepository.findByBranchAndStatusAndCreatedAtBetween(
                                branch, OrderStatus.DELIVERED, startDate, endDate);

                return orders.stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private OrderDTO convertToDTO(Order order) {
                List<OrderItemDTO> orderItems = orderItemRepository.findByOrder(order).stream()
                                .map(this::convertOrderItemToDTO)
                                .collect(Collectors.toList());

                return OrderDTO.builder()
                                .orderId(order.getOrderId())
                                .userId(order.getUser().getUserId())
                                .branchId(order.getBranch().getBranchId())
                                .status(order.getStatus().toString())
                                .totalAmount(order.getTotalAmount())
                                .shippingAddress(order.getShippingAddress())
                                .notes(order.getNotes())
                                .orderItems(orderItems)
                                .createdAt(order.getCreatedAt())
                                .updatedAt(order.getUpdatedAt())
                                .build();
        }

        private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
                return OrderItemDTO.builder()
                                .orderItemId(orderItem.getOrderItemId())
                                .itemId(orderItem.getItem().getItemId())
                                .itemName(orderItem.getItem().getName())
                                .quantity(orderItem.getQuantity())
                                .unitPrice(orderItem.getUnitPrice())
                                .totalPrice(orderItem.getUnitPrice()
                                                .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                                .build();
        }
}
