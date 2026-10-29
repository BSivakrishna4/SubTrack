package com.subtrack.paymentservice.controller;

import com.subtrack.paymentservice.dto.PaymentRequest;
import com.subtrack.paymentservice.dto.PaymentResponse;
import com.subtrack.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPaymentsExplicit() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable("userId") Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable("id") Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
}
