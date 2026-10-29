package com.subtrack.subscriptionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import java.time.LocalDateTime;

public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private PlanDto plan;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;

    private SubscriptionStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private PaymentResponse lastPayment;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(Long id, Long userId, PlanDto plan, LocalDateTime startDate, LocalDateTime expiryDate, SubscriptionStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, PaymentResponse lastPayment) {
        this.id = id;
        this.userId = userId;
        this.plan = plan;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastPayment = lastPayment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PlanDto getPlan() {
        return plan;
    }

    public void setPlan(PlanDto plan) {
        this.plan = plan;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public PaymentResponse getLastPayment() {
        return lastPayment;
    }

    public void setLastPayment(PaymentResponse lastPayment) {
        this.lastPayment = lastPayment;
    }
}
