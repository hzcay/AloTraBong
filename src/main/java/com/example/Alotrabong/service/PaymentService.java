package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Payment;

import java.util.List;

public interface PaymentService {
    
    List<Payment> getPaymentsByOrder(String orderId);
    
    Payment createPayment(Payment payment);
    
    Payment updatePaymentStatus(String paymentId, String status);
}