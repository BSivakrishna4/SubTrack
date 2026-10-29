package com.subtrack.subscriptionservice.client;

import com.subtrack.subscriptionservice.dto.PaymentRequest;
import com.subtrack.subscriptionservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/api/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);
}
