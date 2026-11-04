package com.subtrack.subscriptionservice.controller;

import com.subtrack.subscriptionservice.dto.ActivateRequest;
import com.subtrack.subscriptionservice.dto.RenewRequest;
import com.subtrack.subscriptionservice.dto.SubscriptionResponse;
import com.subtrack.subscriptionservice.exception.AccessDeniedException;
import com.subtrack.subscriptionservice.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @org.springframework.beans.factory.annotation.Value("${internal.gateway-token:}")
    private String internalGatewayToken;

    @org.springframework.beans.factory.annotation.Value("${internal.service-secret:}")
    private String internalServiceSecret;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @jakarta.annotation.PostConstruct
    public void validateCredentials() {
        if (internalGatewayToken == null || internalGatewayToken.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' is not configured. Subscription Service startup aborted.");
        }
        if (internalGatewayToken.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' must be at least 32 bytes (256 bits). Subscription Service startup aborted.");
        }
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' is not configured. Subscription Service startup aborted.");
        }
        if (internalServiceSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' must be at least 32 bytes (256 bits). Subscription Service startup aborted.");
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

    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(
            @Valid @RequestBody ActivateRequest activateRequest,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || authUserId == null || authUserId.trim().isEmpty()) {
            throw new AccessDeniedException("Access denied: Valid user authentication required to activate subscription");
        }

        // Prevent BOLA/IDOR: Overwrite client-supplied userId with verified identity from Gateway
        activateRequest.setUserId(Long.parseLong(authUserId.trim()));

        SubscriptionResponse response = subscriptionService.activateSubscription(activateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all subscriptions");
        }
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptionsExplicit(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all subscriptions");
        }
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to subscription");
        }

        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        
        // Identity Isolation
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(response.getUserId())))) {
            throw new AccessDeniedException("Access denied: You cannot view subscriptions belonging to another user");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to subscriptions");
        }
        
        // Identity Isolation
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(userId)))) {
            throw new AccessDeniedException("Access denied: You cannot view subscriptions belonging to another user");
        }
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @PathVariable("id") Long id,
            @RequestBody(required = false) RenewRequest renewRequest,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to renew subscription");
        }
        
        SubscriptionResponse existing = subscriptionService.getSubscriptionById(id);
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(existing.getUserId())))) {
            throw new AccessDeniedException("Access denied: You cannot renew another user's subscription");
        }

        SubscriptionResponse response = subscriptionService.renewSubscription(id, renewRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to cancel subscription");
        }
        
        SubscriptionResponse existing = subscriptionService.getSubscriptionById(id);
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(existing.getUserId())))) {
            throw new AccessDeniedException("Access denied: You cannot cancel another user's subscription");
        }

        SubscriptionResponse response = subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader)) {
            throw new AccessDeniedException("Access denied: Unauthorized access to delete subscription");
        }
        
        SubscriptionResponse existing = subscriptionService.getSubscriptionById(id);
        if (!"ADMIN".equalsIgnoreCase(authRole) && (authUserId == null || !authUserId.equals(String.valueOf(existing.getUserId())))) {
            throw new AccessDeniedException("Access denied: You cannot delete another user's subscription");
        }

        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
