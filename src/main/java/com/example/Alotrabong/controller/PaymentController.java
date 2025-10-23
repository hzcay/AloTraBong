package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.entity.Payment;
import com.example.Alotrabong.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER', 'USER')")
    @Operation(summary = "Get payments by order")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByOrder(@PathVariable String orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", payments));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER', 'USER')")
    @Operation(summary = "Create payment")
    public ResponseEntity<ApiResponse<Payment>> createPayment(@RequestBody Payment payment) {
        Payment created = paymentService.createPayment(payment);
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", created));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update payment status")
    public ResponseEntity<ApiResponse<Payment>> updatePaymentStatus(
            @PathVariable String id,
            @RequestParam String status) {
        Payment payment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated", payment));
    }
}