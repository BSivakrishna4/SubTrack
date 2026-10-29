package com.subtrack.subscriptionservice.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private Long userId;
    private Long subscriptionId;
    private BigDecimal amount;
    private boolean simulateFailure;

    public PaymentRequest() {
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
