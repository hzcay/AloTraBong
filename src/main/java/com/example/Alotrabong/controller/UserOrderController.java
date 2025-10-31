package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/user/order/confirm-received/{orderId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> confirmReceived(
            @PathVariable String orderId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");
        }

        var userOpt = userRepository.findByLogin(auth.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không tìm thấy người dùng");
        }

        var orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
        }

        var order = orderOpt.get();

        // Chỉ chủ đơn hàng mới được xác nhận
        if (!order.getUser().equals(userOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xác nhận đơn của người khác");
        }

        // Cập nhật trạng thái
        order.setStatus(OrderStatus.RECEIVED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok("success");
    }

}
