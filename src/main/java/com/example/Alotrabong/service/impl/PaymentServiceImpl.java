package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Payment;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.PaymentRepository;
import com.example.Alotrabong.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public Payment createPayment(Payment payment) {
        log.info("Creating payment for order: {}", payment.getOrderId());
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully: {}", savedPayment.getPaymentId());
        return savedPayment;
    }

    @Override
    public Payment updatePaymentStatus(String paymentId, String status) {
        log.info("Updating payment status: {} to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        // TODO: Update payment status logic
        payment = paymentRepository.save(payment);
        log.info("Payment status updated: {}", paymentId);
        return payment;
    }
}
