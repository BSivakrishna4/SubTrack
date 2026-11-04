package com.subtrack.subscriptionservice.service;

import com.subtrack.subscriptionservice.client.PaymentClient;
import com.subtrack.subscriptionservice.client.UserClient;
import com.subtrack.subscriptionservice.dto.ActivateRequest;
import com.subtrack.subscriptionservice.dto.PaymentRequest;
import com.subtrack.subscriptionservice.dto.PaymentResponse;
import com.subtrack.subscriptionservice.dto.RenewRequest;
import com.subtrack.subscriptionservice.dto.SubscriptionResponse;
import com.subtrack.subscriptionservice.dto.UserDto;
import com.subtrack.subscriptionservice.entity.Plan;
import com.subtrack.subscriptionservice.entity.Subscription;
import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import com.subtrack.subscriptionservice.exception.PaymentFailedException;
import com.subtrack.subscriptionservice.exception.ResourceNotFoundException;
import com.subtrack.subscriptionservice.repository.PlanRepository;
import com.subtrack.subscriptionservice.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private UserClient userClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Plan standardPlan;
    private Plan inactivePlan;

    @BeforeEach
    void setUp() {
        standardPlan = new Plan(
                1L,
                "Basic Monthly",
                "Standard 30-day access",
                new BigDecimal("199.00"),
                30,
                true
        );

        inactivePlan = new Plan(
                2L,
                "Legacy Archived Tier",
                "Archived grandfathered plan",
                new BigDecimal("99.00"),
                30,
                false
        );
    }

    // =========================================================================
    // 1. Activation Unit Tests
    // =========================================================================

    @Test
    @DisplayName("activateSubscription: Successful payment activates subscription, computes dates, and returns DTO")
    void activateSubscription_Success() {
        // Arrange
        Long userId = 10L;
        Long planId = 1L;
        ActivateRequest activateRequest = new ActivateRequest(userId, planId, false);

        when(planRepository.findById(planId)).thenReturn(Optional.of(standardPlan));

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription sub = invocation.getArgument(0);
            sub.setId(101L);
            return sub;
        });

        PaymentResponse mockPaymentResponse = new PaymentResponse(
                5001L,
                "TXN-1789568577964-32A145F2",
                "SUCCESS",
                standardPlan.getPrice(),
                userId,
                101L,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(mockPaymentResponse);

        // Act
        SubscriptionResponse response = subscriptionService.activateSubscription(activateRequest);

        // Assert - verify initial subscription entity passed to repository
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).save(subscriptionCaptor.capture());
        Subscription savedEntity = subscriptionCaptor.getValue();

        assertEquals(userId, savedEntity.getUserId());
        assertEquals(planId, savedEntity.getPlanId());
        assertEquals(SubscriptionStatus.ACTIVE, savedEntity.getStatus());
        assertNotNull(savedEntity.getStartDate());
        assertNotNull(savedEntity.getExpiryDate());

        // Verify expiry date calculation: startDate + durationDays (30 days)
        long daysBetween = ChronoUnit.DAYS.between(savedEntity.getStartDate().toLocalDate(), savedEntity.getExpiryDate().toLocalDate());
        assertEquals(standardPlan.getDurationDays().longValue(), daysBetween);

        // Assert - verify PaymentClient called with expected parameters
        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentClient, times(1)).processPayment(paymentCaptor.capture());
        PaymentRequest capturedPayment = paymentCaptor.getValue();

        assertEquals(userId, capturedPayment.getUserId());
        assertEquals(101L, capturedPayment.getSubscriptionId());
        assertEquals(standardPlan.getPrice(), capturedPayment.getAmount());
        assertFalse(capturedPayment.isSimulateFailure());

        // Assert - verify returned DTO structure
        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        assertNotNull(response.getPlan());
        assertEquals("Basic Monthly", response.getPlan().getName());
        assertEquals(new BigDecimal("199.00"), response.getPlan().getPrice());

        assertNotNull(response.getLastPayment());
        assertEquals("SUCCESS", response.getLastPayment().getStatus());
        assertEquals("TXN-1789568577964-32A145F2", response.getLastPayment().getTransactionReference());
        assertEquals(5001L, response.getLastPayment().getPaymentId());

        // Verify no rollback occurred
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("activateSubscription: Payment failure rolls back (deletes) subscription and throws PaymentFailedException")
    void activateSubscription_PaymentFailure_RollsBackSubscription() {
        // Arrange
        Long userId = 10L;
        Long planId = 1L;
        ActivateRequest activateRequest = new ActivateRequest(userId, planId, true);

        when(planRepository.findById(planId)).thenReturn(Optional.of(standardPlan));

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription sub = invocation.getArgument(0);
            sub.setId(102L);
            return sub;
        });

        PaymentResponse failedPaymentResponse = new PaymentResponse(
                5002L,
                "TXN-1789568577964-FAILED",
                "FAILED",
                standardPlan.getPrice(),
                userId,
                102L,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(failedPaymentResponse);

        // Act & Assert
        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> {
            subscriptionService.activateSubscription(activateRequest);
        });

        // Verify exception message contains failure details and transaction reference
        assertThat(exception.getMessage())
                .contains("Payment FAILED")
                .contains("TXN-1789568577964-FAILED");

        // Verify rollback: saved subscription was deleted from repository
        ArgumentCaptor<Subscription> deleteCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).delete(deleteCaptor.capture());
        assertEquals(102L, deleteCaptor.getValue().getId());
    }

    @Test
    @DisplayName("activateSubscription: Nonexistent plan throws ResourceNotFoundException and aborts before persistence")
    void activateSubscription_PlanNotFound() {
        // Arrange
        Long missingPlanId = 999L;
        ActivateRequest activateRequest = new ActivateRequest(10L, missingPlanId, false);

        when(planRepository.findById(missingPlanId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            subscriptionService.activateSubscription(activateRequest);
        });

        assertThat(exception.getMessage()).contains("Plan not found with ID: " + missingPlanId);

        // Verify no subscription was saved and no payment was attempted
        verify(subscriptionRepository, never()).save(any());
        verify(paymentClient, never()).processPayment(any());
    }

    @Test
    @DisplayName("activateSubscription: Inactive plan throws IllegalArgumentException and prevents payment processing")
    void activateSubscription_InactivePlan() {
        // Arrange
        Long inactivePlanId = 2L;
        ActivateRequest activateRequest = new ActivateRequest(10L, inactivePlanId, false);

        when(planRepository.findById(inactivePlanId)).thenReturn(Optional.of(inactivePlan));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.activateSubscription(activateRequest);
        });

        assertThat(exception.getMessage()).contains("Selected plan is not currently active");

        // Verify no subscription was saved and no payment was attempted
        verify(subscriptionRepository, never()).save(any());
        verify(paymentClient, never()).processPayment(any());
    }

    // =========================================================================
    // 2. Renewal Unit Tests
    // =========================================================================

    @Test
    @DisplayName("renewSubscription: Extends expiry from future expiry date, resets notification flags, and preserves ACTIVE status")
    void renewSubscription_Success_ExtendsFromFutureExpiryDate() {
        // Arrange
        Long subscriptionId = 101L;
        Long userId = 10L;
        LocalDateTime futureExpiry = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);

        Subscription existingSub = new Subscription();
        existingSub.setId(subscriptionId);
        existingSub.setUserId(userId);
        existingSub.setPlanId(standardPlan.getId());
        existingSub.setStartDate(LocalDateTime.now().minusDays(20));
        existingSub.setExpiryDate(futureExpiry);
        existingSub.setStatus(SubscriptionStatus.ACTIVE);
        existingSub.setNotified7Days(true);
        existingSub.setNotified3Days(true);
        existingSub.setNotifiedExpired(true);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(existingSub));
        when(planRepository.findById(standardPlan.getId())).thenReturn(Optional.of(standardPlan));

        PaymentResponse paymentResponse = new PaymentResponse(
                6001L,
                "TXN-RENEW-SUCCESS",
                "SUCCESS",
                standardPlan.getPrice(),
                userId,
                subscriptionId,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SubscriptionResponse response = subscriptionService.renewSubscription(subscriptionId, new RenewRequest(false));

        // Assert - verify payment request
        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentClient, times(1)).processPayment(paymentCaptor.capture());
        assertEquals(userId, paymentCaptor.getValue().getUserId());
        assertEquals(subscriptionId, paymentCaptor.getValue().getSubscriptionId());
        assertEquals(standardPlan.getPrice(), paymentCaptor.getValue().getAmount());

        // Assert - verify updated subscription entity passed to save
        ArgumentCaptor<Subscription> subCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).save(subCaptor.capture());
        Subscription saved = subCaptor.getValue();

        // Must extend from the existing future expiry date: futureExpiry + 30 days
        assertEquals(futureExpiry.plusDays(standardPlan.getDurationDays()), saved.getExpiryDate());
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());

        // Notification flags must all be reset to false for the new billing cycle
        assertFalse(saved.isNotified7Days(), "notified7Days must be reset to false on renewal");
        assertFalse(saved.isNotified3Days(), "notified3Days must be reset to false on renewal");
        assertFalse(saved.isNotifiedExpired(), "notifiedExpired must be reset to false on renewal");

        // Verify returned DTO
        assertNotNull(response);
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        assertEquals(futureExpiry.plusDays(standardPlan.getDurationDays()), response.getExpiryDate());
        assertEquals("SUCCESS", response.getLastPayment().getStatus());
    }

    @Test
    @DisplayName("renewSubscription: Extends expiry from current time when existing expiry is in the past")
    void renewSubscription_Success_ExtendsFromCurrentTimeWhenAlreadyExpired() {
        // Arrange
        Long subscriptionId = 102L;
        Long userId = 10L;
        LocalDateTime pastExpiry = LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.SECONDS);

        Subscription expiredSub = new Subscription();
        expiredSub.setId(subscriptionId);
        expiredSub.setUserId(userId);
        expiredSub.setPlanId(standardPlan.getId());
        expiredSub.setStartDate(LocalDateTime.now().minusDays(35));
        expiredSub.setExpiryDate(pastExpiry);
        expiredSub.setStatus(SubscriptionStatus.EXPIRED);
        expiredSub.setNotifiedExpired(true);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(expiredSub));
        when(planRepository.findById(standardPlan.getId())).thenReturn(Optional.of(standardPlan));

        PaymentResponse paymentResponse = new PaymentResponse(
                6002L,
                "TXN-RENEW-PAST-SUCCESS",
                "SUCCESS",
                standardPlan.getPrice(),
                userId,
                subscriptionId,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SubscriptionResponse response = subscriptionService.renewSubscription(subscriptionId, new RenewRequest(false));

        // Assert - verify extension is based on current time (now + 30 days), NOT pastExpiry + 30 days
        ArgumentCaptor<Subscription> subCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).save(subCaptor.capture());
        Subscription saved = subCaptor.getValue();

        long daysFromToday = ChronoUnit.DAYS.between(LocalDate.now(), saved.getExpiryDate().toLocalDate());
        assertEquals(standardPlan.getDurationDays().longValue(), daysFromToday, "Expiry must extend 30 days from now");
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus(), "Status must transition back to ACTIVE");
        assertFalse(saved.isNotifiedExpired());
    }

    @Test
    @DisplayName("renewSubscription: Payment failure throws PaymentFailedException and does not persist changes")
    void renewSubscription_PaymentFailure_ThrowsExceptionAndDoesNotPersist() {
        // Arrange
        Long subscriptionId = 103L;
        Subscription existingSub = new Subscription();
        existingSub.setId(subscriptionId);
        existingSub.setUserId(10L);
        existingSub.setPlanId(standardPlan.getId());
        existingSub.setStatus(SubscriptionStatus.ACTIVE);
        existingSub.setExpiryDate(LocalDateTime.now().plusDays(2));

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(existingSub));
        when(planRepository.findById(standardPlan.getId())).thenReturn(Optional.of(standardPlan));

        PaymentResponse failedPayment = new PaymentResponse(
                6003L,
                "TXN-RENEW-FAILED",
                "FAILED",
                standardPlan.getPrice(),
                10L,
                subscriptionId,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(failedPayment);

        // Act & Assert
        PaymentFailedException ex = assertThrows(PaymentFailedException.class, () -> {
            subscriptionService.renewSubscription(subscriptionId, new RenewRequest(true));
        });

        assertThat(ex.getMessage()).contains("Renewal payment FAILED");

        // Existing subscription must NOT be updated or deleted in repository
        verify(subscriptionRepository, never()).save(any());
        verify(subscriptionRepository, never()).delete(any());
    }

    // =========================================================================
    // 3. Expiry Scheduler & Lifecycle Transition Unit Tests
    // =========================================================================

    @Test
    @DisplayName("checkAndExpireSubscriptions: Transitions overdue ACTIVE subscriptions to EXPIRED")
    void checkAndExpireSubscriptions_TransitionsOverdueActiveToExpired() {
        // Arrange - overdue ACTIVE subscription
        Subscription overdueSub = new Subscription();
        overdueSub.setId(201L);
        overdueSub.setUserId(10L);
        overdueSub.setPlanId(1L);
        overdueSub.setStatus(SubscriptionStatus.ACTIVE);
        overdueSub.setExpiryDate(LocalDateTime.now().minusHours(2));

        when(subscriptionRepository.findByStatusAndExpiryDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueSub));
        when(subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        // Act
        subscriptionService.checkAndExpireSubscriptions();

        // Assert - verify status was changed to EXPIRED and saved
        ArgumentCaptor<Subscription> subCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).save(subCaptor.capture());
        Subscription saved = subCaptor.getValue();

        assertEquals(201L, saved.getId());
        assertEquals(SubscriptionStatus.EXPIRED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("checkAndExpireSubscriptions: Ignores unexpired ACTIVE subscriptions and non-ACTIVE statuses")
    void checkAndExpireSubscriptions_IgnoresUnexpiredAndNonActiveSubscriptions() {
        // When query for overdue ACTIVE subscriptions returns empty list
        when(subscriptionRepository.findByStatusAndExpiryDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        // Act
        subscriptionService.checkAndExpireSubscriptions();

        // Assert - no entity is saved for expiration
        verify(subscriptionRepository, never()).save(any());
    }

    // =========================================================================
    // 4. Reminder and Notification Deduplication Unit Tests
    // =========================================================================

    @Test
    @DisplayName("checkAndExpireSubscriptions: Sends expiry notification for EXPIRED subscription and sets notifiedExpired=true")
    void checkAndExpireSubscriptions_SendsExpiredNotificationAndDeduplicates() {
        // Arrange
        Subscription expiredSub = new Subscription();
        expiredSub.setId(301L);
        expiredSub.setUserId(15L);
        expiredSub.setPlanId(1L);
        expiredSub.setStatus(SubscriptionStatus.EXPIRED);
        expiredSub.setExpiryDate(LocalDateTime.now().minusDays(1));
        expiredSub.setNotifiedExpired(false);

        when(subscriptionRepository.findByStatusAndExpiryDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED))
                .thenReturn(List.of(expiredSub));
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        when(userClient.getUserById(15L)).thenReturn(new UserDto(15L, "Alice Subscriber", "alice@subtrack.com", "USER"));
        when(planRepository.findById(1L)).thenReturn(Optional.of(standardPlan));
        when(emailService.sendExpiredNotification(eq("alice@subtrack.com"), anyString(), anyString(), any(), eq(301L)))
                .thenReturn(true);

        // Act
        subscriptionService.checkAndExpireSubscriptions();

        // Assert
        verify(emailService, times(1)).sendExpiredNotification(
                eq("alice@subtrack.com"),
                eq("Alice Subscriber"),
                eq("Basic Monthly"),
                any(),
                eq(301L)
        );

        ArgumentCaptor<Subscription> subCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(1)).save(subCaptor.capture());
        assertTrue(subCaptor.getValue().isNotifiedExpired(), "notifiedExpired flag must be set to true to prevent duplicates");
    }

    @Test
    @DisplayName("checkAndExpireSubscriptions: Sends 7-day and 3-day reminders and updates notification flags")
    void checkAndExpireSubscriptions_Sends7DayAnd3DayRemindersAndSetsFlags() {
        // Arrange - 7-day reminder candidate (expiry 6 calendar days in future)
        Subscription sub7Day = new Subscription();
        sub7Day.setId(401L);
        sub7Day.setUserId(16L);
        sub7Day.setPlanId(1L);
        sub7Day.setStatus(SubscriptionStatus.ACTIVE);
        sub7Day.setExpiryDate(LocalDateTime.now().plusDays(6));
        sub7Day.setNotified7Days(false);
        sub7Day.setNotified3Days(false);

        // Arrange - 3-day reminder candidate (expiry 2 calendar days in future)
        Subscription sub3Day = new Subscription();
        sub3Day.setId(402L);
        sub3Day.setUserId(17L);
        sub3Day.setPlanId(1L);
        sub3Day.setStatus(SubscriptionStatus.ACTIVE);
        sub3Day.setExpiryDate(LocalDateTime.now().plusDays(2));
        sub3Day.setNotified7Days(false);
        sub3Day.setNotified3Days(false);

        when(subscriptionRepository.findByStatusAndExpiryDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(sub7Day, sub3Day));

        when(userClient.getUserById(16L)).thenReturn(new UserDto(16L, "Bob Subscriber", "bob@subtrack.com", "USER"));
        when(userClient.getUserById(17L)).thenReturn(new UserDto(17L, "Charlie Subscriber", "charlie@subtrack.com", "USER"));
        when(planRepository.findById(1L)).thenReturn(Optional.of(standardPlan));

        when(emailService.send7DaysExpiryNotification(anyString(), anyString(), anyString(), any(), anyLong())).thenReturn(true);
        when(emailService.send3DaysExpiryNotification(anyString(), anyString(), anyString(), any(), anyLong())).thenReturn(true);

        // Act
        subscriptionService.checkAndExpireSubscriptions();

        // Assert - verify 7-day email was sent for sub 401
        verify(emailService, times(1)).send7DaysExpiryNotification(
                eq("bob@subtrack.com"),
                eq("Bob Subscriber"),
                eq("Basic Monthly"),
                any(),
                eq(401L)
        );
        assertTrue(sub7Day.isNotified7Days(), "notified7Days must be updated to true");

        // Assert - verify 3-day email was sent for sub 402
        verify(emailService, times(1)).send3DaysExpiryNotification(
                eq("charlie@subtrack.com"),
                eq("Charlie Subscriber"),
                eq("Basic Monthly"),
                any(),
                eq(402L)
        );
        assertTrue(sub3Day.isNotified3Days(), "notified3Days must be updated to true");
        assertTrue(sub3Day.isNotified7Days(), "notified7Days must also be marked true when 3-day notification fires");
    }

    @Test
    @DisplayName("checkAndExpireSubscriptions: Deduplication skips reminders when flags are already set to true")
    void checkAndExpireSubscriptions_DeduplicationSkipsRemindersWhenFlagsAlreadySet() {
        // Arrange - subscription within 7-day window, but notified7Days is ALREADY true
        Subscription subAlreadyNotified = new Subscription();
        subAlreadyNotified.setId(501L);
        subAlreadyNotified.setUserId(18L);
        subAlreadyNotified.setPlanId(1L);
        subAlreadyNotified.setStatus(SubscriptionStatus.ACTIVE);
        subAlreadyNotified.setExpiryDate(LocalDateTime.now().plusDays(5));
        subAlreadyNotified.setNotified7Days(true); // already sent
        subAlreadyNotified.setNotified3Days(false);

        when(subscriptionRepository.findByStatusAndExpiryDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(subAlreadyNotified));

        // Act
        subscriptionService.checkAndExpireSubscriptions();

        // Assert - neither 7-day nor 3-day email should be dispatched
        verify(emailService, never()).send7DaysExpiryNotification(anyString(), anyString(), anyString(), any(), anyLong());
        verify(emailService, never()).send3DaysExpiryNotification(anyString(), anyString(), anyString(), any(), anyLong());
        verify(userClient, never()).getUserById(anyLong());
    }
}
