package com.subtrack.subscriptionservice.controller;

import com.subtrack.subscriptionservice.dto.PlanDto;
import com.subtrack.subscriptionservice.exception.AccessDeniedException;
import com.subtrack.subscriptionservice.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    @org.springframework.beans.factory.annotation.Value("${internal.gateway-token:}")
    private String internalGatewayToken;

    @org.springframework.beans.factory.annotation.Value("${internal.service-secret:}")
    private String internalServiceSecret;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @jakarta.annotation.PostConstruct
    public void validateCredentials() {
        if (internalGatewayToken == null || internalGatewayToken.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' is not configured. Subscription Service PlanController startup aborted.");
        }
        if (internalGatewayToken.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' must be at least 32 bytes (256 bits). Subscription Service PlanController startup aborted.");
        }
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' is not configured. Subscription Service PlanController startup aborted.");
        }
        if (internalServiceSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' must be at least 32 bytes (256 bits). Subscription Service PlanController startup aborted.");
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

    @GetMapping
    public ResponseEntity<List<PlanDto>> getActivePlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlanDto>> getAllPlans(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all plans");
        }
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<PlanDto> createPlan(
            @Valid @RequestBody PlanDto planDto,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Only administrators can create subscription plans");
        }
        PlanDto createdPlan = planService.createPlan(planDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable("id") Long id,
            @Valid @RequestBody PlanDto planDto,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Only administrators can modify subscription plans");
        }
        PlanDto updatedPlan = planService.updatePlan(id, planDto);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader) {
        if (!isTrustedCaller(gatewayToken, internalServiceHeader) || !"ADMIN".equalsIgnoreCase(authRole)) {
            throw new AccessDeniedException("Access denied: Only administrators can delete subscription plans");
        }
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
