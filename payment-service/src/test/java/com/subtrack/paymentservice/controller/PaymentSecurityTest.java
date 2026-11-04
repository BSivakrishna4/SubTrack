package com.subtrack.paymentservice.controller;

import com.subtrack.paymentservice.dto.PaymentRequest;
import com.subtrack.paymentservice.dto.PaymentResponse;
import com.subtrack.paymentservice.exception.AccessDeniedException;
import com.subtrack.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentSecurityTest {

    private static final String GATEWAY_TOKEN = "test-gateway-token-for-payment-service-32bytes!";
    private static final String INTERNAL_SERVICE_SECRET = "test-internal-secret-for-payment-service-32bytes!";

    private PaymentService paymentService;
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        paymentController = new PaymentController(paymentService);
        paymentController.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
    }

    private PaymentResponse createMockPayment(Long id, Long userId) {
        return new PaymentResponse(
                id,
                "TXN-12345-ABCD",
                "SUCCESS",
                new BigDecimal("199.00"),
                userId,
                1L,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Internal Token Fail-Fast: Missing or short internal tokens in PaymentController throw IllegalStateException")
    void testPaymentController_ValidateCredentials_FailFast() {
        PaymentController c = new PaymentController(paymentService);

        c.setSecurityTokens("", INTERNAL_SERVICE_SECRET);
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens("short-token", INTERNAL_SERVICE_SECRET);
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, "");
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, "short-secret");
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
        c.validateCredentials(); // Should pass
    }

    @Test
    @DisplayName("Direct Port Bypass Prevention: Calling processPayment directly without internal service secret is forbidden")
    void testProcessPayment_ExternalCallerWithoutInternalSecret_Forbidden() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("199.00"));

        assertThrows(AccessDeniedException.class, () -> {
            paymentController.processPayment(request, null);
        });
        verify(paymentService, never()).processPayment(any());
    }

    @Test
    @DisplayName("Direct Port Bypass Prevention: Calling processPayment with invalid secret is forbidden")
    void testProcessPayment_InvalidInternalSecret_Forbidden() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("199.00"));

        assertThrows(AccessDeniedException.class, () -> {
            paymentController.processPayment(request, "wrong-secret-that-does-not-match-at-all!");
        });
        verify(paymentService, never()).processPayment(any());
    }

    @Test
    @DisplayName("Trusted Inter-Service Call: Authorized Feign call with valid internal secret processes payment")
    void testProcessPayment_ValidInternalSecret_Success() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("199.00"));
        when(paymentService.processPayment(any())).thenReturn(createMockPayment(10L, 1L));

        ResponseEntity<PaymentResponse> response = paymentController.processPayment(request, INTERNAL_SERVICE_SECRET);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(paymentService, times(1)).processPayment(any());
    }

    @Test
    @DisplayName("Direct Port Access Prevention: Spoofed identity headers without valid Gateway token are rejected")
    void testGetPaymentsByUserId_SpoofedHeadersWithoutGatewayToken_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            paymentController.getPaymentsByUserId(1L, "1", "USER", null, null);
        });
        verify(paymentService, never()).getPaymentsByUserId(any());
    }

    @Test
    @DisplayName("Identity Isolation: User A cannot view User B's payments through Gateway")
    void testGetPaymentsByUserId_DifferentUser_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            paymentController.getPaymentsByUserId(2L, "1", "USER", GATEWAY_TOKEN, null);
        });
        verify(paymentService, never()).getPaymentsByUserId(any());
    }

    @Test
    @DisplayName("Identity Isolation: User can view their own payments through Gateway")
    void testGetPaymentsByUserId_Owner_Success() {
        when(paymentService.getPaymentsByUserId(1L)).thenReturn(List.of(createMockPayment(1L, 1L)));

        ResponseEntity<List<PaymentResponse>> response =
                paymentController.getPaymentsByUserId(1L, "1", "USER", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(paymentService, times(1)).getPaymentsByUserId(1L);
    }

    @Test
    @DisplayName("Admin Access: Admin can view any user's payments through Gateway")
    void testGetPaymentsByUserId_Admin_Success() {
        when(paymentService.getPaymentsByUserId(2L)).thenReturn(List.of(createMockPayment(1L, 2L)));

        ResponseEntity<List<PaymentResponse>> response =
                paymentController.getPaymentsByUserId(2L, "99", "ADMIN", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Admin Spoofing Prevention: Non-admin or spoofed caller cannot view all payments")
    void testGetAllPayments_NonAdmin_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            paymentController.getAllPaymentsExplicit("USER", GATEWAY_TOKEN, null);
        });
        verify(paymentService, never()).getAllPayments();
    }

    @Test
    @DisplayName("Admin RBAC: Authenticated admin through Gateway can view all payments")
    void testGetAllPayments_Admin_Success() {
        when(paymentService.getAllPayments()).thenReturn(List.of(createMockPayment(1L, 1L)));

        ResponseEntity<List<PaymentResponse>> response =
                paymentController.getAllPaymentsExplicit("ADMIN", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
