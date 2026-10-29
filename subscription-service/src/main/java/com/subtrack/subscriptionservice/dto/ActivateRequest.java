package com.subtrack.subscriptionservice.dto;

import jakarta.validation.constraints.NotNull;

public class ActivateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private boolean simulatePaymentFailure = false;

    public ActivateRequest() {
    }

    public ActivateRequest(Long userId, Long planId) {
        this.userId = userId;
        this.planId = planId;
    }

    public ActivateRequest(Long userId, Long planId, boolean simulatePaymentFailure) {
        this.userId = userId;
        this.planId = planId;
        this.simulatePaymentFailure = simulatePaymentFailure;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public boolean isSimulatePaymentFailure() {
        return simulatePaymentFailure;
    }

    public void setSimulatePaymentFailure(boolean simulatePaymentFailure) {
        this.simulatePaymentFailure = simulatePaymentFailure;
    }
}
