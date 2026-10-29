package com.subtrack.paymentservice.service;

import com.subtrack.paymentservice.dto.PaymentRequest;
import com.subtrack.paymentservice.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    List<PaymentResponse> getPaymentsByUserId(Long userId);
    List<PaymentResponse> getAllPayments();
    PaymentResponse getPaymentById(Long paymentId);
}
