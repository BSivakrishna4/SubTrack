package com.subtrack.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private boolean simulateFailure = false;

    public PaymentRequest() {
    }

    public PaymentRequest(Long userId, Long subscriptionId, BigDecimal amount) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
    }

    public PaymentRequest(Long userId, Long subscriptionId, BigDecimal amount, boolean simulateFailure) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.simulateFailure = simulateFailure;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isSimulateFailure() {
        return simulateFailure;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
