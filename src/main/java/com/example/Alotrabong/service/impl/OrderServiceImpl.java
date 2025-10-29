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
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;
    // private final PaymentRepository paymentRepository; // TODO: Implement payment logic

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

        // Kiểm tra tồn kho có đủ nguyên liệu không (chưa trừ)
        for (CartItem cartItem : cartItems) {
            Inventory inventory = inventoryRepository
                    .findByBranch_BranchIdAndItem_ItemId(branch.getBranchId(), cartItem.getItem().getItemId())
                    .orElseThrow(() -> new BadRequestException("Item not available in this branch: " + cartItem.getItem().getName()));
            
            if (inventory.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient inventory for item: " + cartItem.getItem().getName() + 
                    ". Available: " + inventory.getQuantity() + ", Requested: " + cartItem.getQuantity());
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Handle address - try to parse as ID first, fallback to text
        Address addressSnapshot = null;
        String shippingAddressText = request.getShippingAddress();
        
        if (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty()) {
            try {
                // Try to parse as address ID
                Integer addressId = Integer.parseInt(request.getShippingAddress());
                addressSnapshot = addressRepository.findById(addressId).orElse(null);
                if (addressSnapshot != null) {
                    // Build full address text from Address entity
                    shippingAddressText = String.format("%s, %s, %s - %s (%s)", 
                        addressSnapshot.getAddressLine(),
                        addressSnapshot.getDistrict(),
                        addressSnapshot.getCity(),
                        addressSnapshot.getReceiverName(),
                        addressSnapshot.getPhone()
                    );
                }
            } catch (Exception e) {
                // If parsing as ID fails, treat as plain text address
                log.debug("Address not found as ID, treating as text: {}", request.getShippingAddress());
            }
        }

        // Create order
        Order order = Order.builder()
            .user(user)
            .branch(branch)
            .status(OrderStatus.PENDING)
            .paymentMethod(PaymentMethod.COD)
            .totalAmount(totalAmount)
            .addressSnapshot(addressSnapshot) // Set the Address entity
            .shippingAddress(shippingAddressText) // Set the text representation
            .notes(request.getNotes())
            .build();

        order = orderRepository.saveAndFlush(order);

        // Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(cartItem.getItem())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Clear cart
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
        //         .order(order)
        //         .status(status)
        //         .notes("Status updated to " + status)
        //         .build();
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
                .totalPrice(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }
}
