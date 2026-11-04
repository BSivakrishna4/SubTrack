package com.subtrack.paymentservice.service;

import com.subtrack.paymentservice.dto.PaymentRequest;
import com.subtrack.paymentservice.dto.PaymentResponse;
import com.subtrack.paymentservice.entity.Payment;
import com.subtrack.paymentservice.exception.ResourceNotFoundException;
import com.subtrack.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Long userId;
    private Long subscriptionId;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        userId = 42L;
        subscriptionId = 100L;
        amount = new BigDecimal("199.00");
    }

    @Test
    @DisplayName("processPayment: Successful payment persists record with status SUCCESS and returns response")
    void processPayment_Success_PersistsPaymentWithSuccessStatus() {
        // Arrange
        PaymentRequest request = new PaymentRequest(userId, subscriptionId, amount, false);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1001L);
            return p;
        });

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert - verify entity passed to repository.save()
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(captor.capture());
        Payment saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(subscriptionId, saved.getSubscriptionId());
        assertEquals(amount, saved.getAmount());
        assertEquals("SUCCESS", saved.getPaymentStatus());
        assertNotNull(saved.getTransactionReference());
        assertTrue(saved.getTransactionReference().startsWith("TXN-"));
        assertNotNull(saved.getPaymentDate());

        // Assert - verify returned response
        assertNotNull(response);
        assertEquals(1001L, response.getPaymentId());
        assertEquals(userId, response.getUserId());
        assertEquals(subscriptionId, response.getSubscriptionId());
        assertEquals(amount, response.getAmount());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(saved.getTransactionReference(), response.getTransactionReference());
        assertEquals(saved.getPaymentDate(), response.getPaymentDate());
    }

    @Test
    @DisplayName("processPayment: Simulated payment failure persists audit record with status FAILED")
    void processPayment_SimulateFailure_PersistsPaymentWithFailedStatus() {
        // Arrange
        PaymentRequest request = new PaymentRequest(userId, subscriptionId, amount, true);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1002L);
            return p;
        });

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert - verify failed payment IS persisted to database for audit trail
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(captor.capture());
        Payment saved = captor.getValue();

        assertEquals("FAILED", saved.getPaymentStatus());
        assertEquals(userId, saved.getUserId());
        assertEquals(subscriptionId, saved.getSubscriptionId());
        assertEquals(amount, saved.getAmount());
        assertNotNull(saved.getTransactionReference());
        assertTrue(saved.getTransactionReference().startsWith("TXN-"));

        // Assert - response reflects failure
        assertNotNull(response);
        assertEquals(1002L, response.getPaymentId());
        assertEquals("FAILED", response.getStatus());
    }

    @Test
    @DisplayName("processPayment: Transaction reference adheres to 'TXN-<timestamp>-<UUID8>' format and is unique")
    void processPayment_TransactionReference_HasValidFormatAndIsUnique() {
        // Arrange
        PaymentRequest request1 = new PaymentRequest(userId, subscriptionId, amount, false);
        PaymentRequest request2 = new PaymentRequest(userId, subscriptionId, amount, false);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentResponse response1 = paymentService.processPayment(request1);
        PaymentResponse response2 = paymentService.processPayment(request2);

        // Assert - format pattern: TXN-<digits>-<8 uppercase hex characters>
        String txnPattern = "^TXN-\\d+-[0-9A-F]{8}$";
        assertThat(response1.getTransactionReference())
                .as("Transaction reference 1 format")
                .matches(txnPattern);

        assertThat(response2.getTransactionReference())
                .as("Transaction reference 2 format")
                .matches(txnPattern);

        // Assert uniqueness
        assertNotEquals(response1.getTransactionReference(), response2.getTransactionReference(),
                "Each transaction must receive a distinct transaction reference");
    }

    @Test
    @DisplayName("processPayment: Verifies all persisted entity fields are correctly assigned before saving")
    void processPayment_VerifiesAllPersistedFields() {
        // Arrange
        LocalDateTime beforeExecution = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1);
        PaymentRequest request = new PaymentRequest(77L, 200L, new BigDecimal("499.50"), false);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        paymentService.processPayment(request);

        // Assert
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(captor.capture());
        Payment entity = captor.getValue();

        assertEquals(77L, entity.getUserId());
        assertEquals(200L, entity.getSubscriptionId());
        assertEquals(new BigDecimal("499.50"), entity.getAmount());
        assertEquals("SUCCESS", entity.getPaymentStatus());
        assertNotNull(entity.getTransactionReference());
        assertNotNull(entity.getPaymentDate());
        assertFalse(entity.getPaymentDate().isBefore(beforeExecution),
                "Payment date should be truncated current timestamp");
    }

    @Test
    @DisplayName("getPaymentById: Existing payment returns mapped response")
    void getPaymentById_ExistingId_ReturnsResponse() {
        // Arrange
        Payment payment = new Payment(
                555L,
                userId,
                subscriptionId,
                amount,
                "SUCCESS",
                "TXN-EXISTING-12345",
                LocalDateTime.now()
        );
        when(paymentRepository.findById(555L)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPaymentById(555L);

        // Assert
        assertNotNull(response);
        assertEquals(555L, response.getPaymentId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("TXN-EXISTING-12345", response.getTransactionReference());
        assertEquals(userId, response.getUserId());
        assertEquals(subscriptionId, response.getSubscriptionId());
    }

    @Test
    @DisplayName("getPaymentById: Nonexistent payment ID throws ResourceNotFoundException")
    void getPaymentById_NonexistentId_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById(999L);
        });

        assertThat(ex.getMessage()).contains("Payment record not found with ID: 999");
    }

    @Test
    @DisplayName("getPaymentsByUserId: Returns list of mapped payments for user")
    void getPaymentsByUserId_ReturnsUserPaymentsList() {
        // Arrange
        Payment p1 = new Payment(1L, userId, 10L, new BigDecimal("199.00"), "SUCCESS", "TXN-1", LocalDateTime.now());
        Payment p2 = new Payment(2L, userId, 11L, new BigDecimal("499.00"), "FAILED", "TXN-2", LocalDateTime.now());
        when(paymentRepository.findByUserId(userId)).thenReturn(List.of(p1, p2));

        // Act
        List<PaymentResponse> results = paymentService.getPaymentsByUserId(userId);

        // Assert
        assertEquals(2, results.size());
        assertEquals("SUCCESS", results.get(0).getStatus());
        assertEquals("FAILED", results.get(1).getStatus());
    }
}

