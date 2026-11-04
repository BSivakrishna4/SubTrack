package com.subtrack.subscriptionservice.controller;

import com.subtrack.subscriptionservice.config.FeignClientConfig;
import com.subtrack.subscriptionservice.dto.ActivateRequest;
import com.subtrack.subscriptionservice.dto.PlanDto;
import com.subtrack.subscriptionservice.dto.SubscriptionResponse;
import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import com.subtrack.subscriptionservice.exception.AccessDeniedException;
import com.subtrack.subscriptionservice.service.PlanService;
import com.subtrack.subscriptionservice.service.SubscriptionService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionSecurityTest {

    private static final String GATEWAY_TOKEN = "test-gateway-token-for-subscription-service-32bytes!";
    private static final String INTERNAL_SERVICE_SECRET = "test-internal-secret-for-subscription-service-32bytes!";

    private SubscriptionService subscriptionService;
    private PlanService planService;
    private SubscriptionController subscriptionController;
    private PlanController planController;

    @BeforeEach
    void setUp() {
        subscriptionService = mock(SubscriptionService.class);
        planService = mock(PlanService.class);
        subscriptionController = new SubscriptionController(subscriptionService);
        planController = new PlanController(planService);

        subscriptionController.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
        planController.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
    }

    private SubscriptionResponse createMockSubResponse(Long id, Long userId) {
        return new SubscriptionResponse(
                id,
                userId,
                new PlanDto(1L, "Basic", "Basic Plan", new BigDecimal("199.00"), 30, true),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    @Test
    @DisplayName("Internal Token Fail-Fast: Missing or short internal tokens in SubscriptionController throw IllegalStateException")
    void testSubscriptionController_ValidateCredentials_FailFast() {
        SubscriptionController c = new SubscriptionController(subscriptionService);

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
    @DisplayName("Direct Port Access Prevention: Spoofed identity headers without valid Gateway token are rejected")
    void testActivateSubscription_SpoofedHeadersWithoutGatewayToken_Forbidden() {
        ActivateRequest request = new ActivateRequest(999L, 1L);

        assertThrows(AccessDeniedException.class, () -> {
            subscriptionController.activateSubscription(request, "5", null, null);
        });
        verify(subscriptionService, never()).activateSubscription(any());
    }

    @Test
    @DisplayName("BOLA Prevention: Overrides client-supplied userId with authenticated X-User-Id header when Gateway token is valid")
    void testActivateSubscription_OverridesClientUserId() {
        ActivateRequest request = new ActivateRequest(999L, 1L); // Malicious client supplies userId=999
        when(subscriptionService.activateSubscription(any())).thenReturn(createMockSubResponse(10L, 5L));

        ResponseEntity<SubscriptionResponse> response =
                subscriptionController.activateSubscription(request, "5", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(5L, request.getUserId(), "UserId must be overwritten with authenticated identity 5");
    }

    @Test
    @DisplayName("Identity Isolation: User A cannot access User B's subscriptions even with valid Gateway token")
    void testGetSubscriptionsByUserId_DifferentUser_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            subscriptionController.getSubscriptionsByUserId(2L, "1", "USER", GATEWAY_TOKEN, null);
        });
        verify(subscriptionService, never()).getSubscriptionsByUserId(any());
    }

    @Test
    @DisplayName("Admin Access: Admin can view any user's subscriptions through Gateway")
    void testGetSubscriptionsByUserId_Admin_Success() {
        when(subscriptionService.getSubscriptionsByUserId(2L)).thenReturn(List.of(createMockSubResponse(1L, 2L)));

        ResponseEntity<List<SubscriptionResponse>> response =
                subscriptionController.getSubscriptionsByUserId(2L, "99", "ADMIN", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Admin Spoofing Prevention: Caller providing X-User-Role: ADMIN without Gateway token is rejected")
    void testGetAllSubscriptions_SpoofedAdminRole_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            subscriptionController.getAllSubscriptions("ADMIN", null, null);
        });
        verify(subscriptionService, never()).getAllSubscriptions();
    }

    @Test
    @DisplayName("Subscription Ownership: User cannot cancel another user's subscription")
    void testCancelSubscription_DifferentUser_Forbidden() {
        when(subscriptionService.getSubscriptionById(10L)).thenReturn(createMockSubResponse(10L, 2L)); // Owned by user 2

        assertThrows(AccessDeniedException.class, () -> {
            subscriptionController.cancelSubscription(10L, "1", "USER", GATEWAY_TOKEN, null);
        });
        verify(subscriptionService, never()).cancelSubscription(any());
    }

    @Test
    @DisplayName("Subscription Ownership: User can cancel their own subscription")
    void testCancelSubscription_Owner_Success() {
        when(subscriptionService.getSubscriptionById(10L)).thenReturn(createMockSubResponse(10L, 1L)); // Owned by user 1
        when(subscriptionService.cancelSubscription(10L)).thenReturn(createMockSubResponse(10L, 1L));

        ResponseEntity<SubscriptionResponse> response =
                subscriptionController.cancelSubscription(10L, "1", "USER", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(subscriptionService, times(1)).cancelSubscription(10L);
    }

    @Test
    @DisplayName("Admin RBAC: Non-admin cannot create new subscription plan")
    void testCreatePlan_RegularUser_Forbidden() {
        PlanDto planDto = new PlanDto(null, "VIP", "VIP Plan", new BigDecimal("999.00"), 365, true);

        assertThrows(AccessDeniedException.class, () -> {
            planController.createPlan(planDto, "USER", GATEWAY_TOKEN, null);
        });
        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("Admin RBAC: Admin can create new subscription plan through Gateway")
    void testCreatePlan_Admin_Success() {
        PlanDto planDto = new PlanDto(null, "VIP", "VIP Plan", new BigDecimal("999.00"), 365, true);
        when(planService.createPlan(any())).thenReturn(planDto);

        ResponseEntity<PlanDto> response = planController.createPlan(planDto, "ADMIN", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(planService, times(1)).createPlan(any());
    }

    @Test
    @DisplayName("Feign Inter-Service Interceptor: Injects X-Internal-Service header onto outbound Feign requests")
    void testFeignClientConfig_AttachesInternalServiceHeader() {
        FeignClientConfig config = new FeignClientConfig();
        ReflectionTestUtils.setField(config, "internalServiceSecret", "test-feign-secret-at-least-32-bytes-long!");

        config.validateSecret(); // Validate passes

        RequestInterceptor interceptor = config.internalServiceRequestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Collection<String> headerValues = template.headers().get("X-Internal-Service");
        assertNotNull(headerValues);
        assertTrue(headerValues.contains("test-feign-secret-at-least-32-bytes-long!"));
    }

    @Test
    @DisplayName("Feign Client Fail-Fast: Missing or short internal service secret throws IllegalStateException")
    void testFeignClientConfig_ValidateSecret_FailFast() {
        FeignClientConfig config = new FeignClientConfig();

        config.setInternalServiceSecret("");
        assertThrows(IllegalStateException.class, config::validateSecret);

        config.setInternalServiceSecret("short-secret");
        assertThrows(IllegalStateException.class, config::validateSecret);

        config.setInternalServiceSecret("test-feign-secret-at-least-32-bytes-long!");
        config.validateSecret();
    }
}
