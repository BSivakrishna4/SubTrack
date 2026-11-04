package com.subtrack.paymentservice.controller;

import com.subtrack.paymentservice.dto.PaymentRequest;
import com.subtrack.paymentservice.dto.PaymentResponse;
import com.subtrack.paymentservice.exception.AccessDeniedException;
import com.subtrack.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${internal.gateway-token:}")
    private String internalGatewayToken;

    @Value("${internal.service-secret:}")
    private String internalServiceSecret;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @jakarta.annotation.PostConstruct
    public void validateCredentials() {
        if (internalGatewayToken == null || internalGatewayToken.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' is not configured. Payment Service startup aborted.");
        }
        if (internalGatewayToken.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' must be at least 32 bytes (256 bits). Payment Service startup aborted.");
        }
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' is not configured. Payment Service startup aborted.");
        }
        if (internalServiceSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' must be at least 32 bytes (256 bits). Payment Service startup aborted.");
        }
    }

    public void setSecurityTokens(String internalGatewayToken, String internalServiceSecret) {
        this.internalGatewayToken = internalGatewayToken;
        this.internalServiceSecret = internalServiceSecret;
    }

    private boolean isTrustedCaller(String gatewayToken, String internalServiceHeader) {
        if (internalGatewayToken != null && !internalGatewayToken.trim().isEmpty() && internalGatewayToken.equals(gatewayToken)) {
            return true;
        }
        if (internalServiceSecret != null && !internalServiceSecret.trim().isEmpty() && internalServiceSecret.equals(internalServiceHeader)) {
            return true;
        }
        return false;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty() || !internalServiceSecret.equals(internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Direct payment processing is restricted to authorized internal microservices");
        }

        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all payment logs");
        }
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPaymentsExplicit(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all payment logs");
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to payment records");
        }

        // Identity Isolation: Regular users can only view their own payment transactions
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(userId)))) {
            throw new AccessDeniedException("Access denied: You cannot view payment records belonging to another user");
        }

        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to payment records");
        }

        PaymentResponse payment = paymentService.getPaymentById(id);

        // Identity Isolation
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(payment.getUserId())))) {
            throw new AccessDeniedException("Access denied: You cannot view payment records belonging to another user");
        }

        return ResponseEntity.ok(payment);
    }
}
