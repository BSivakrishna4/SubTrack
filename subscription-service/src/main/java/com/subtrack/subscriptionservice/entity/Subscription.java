package com.subtrack.subscriptionservice.entity;

import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "start_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startDate;

    @Column(name = "expiry_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "notified_7_days", nullable = false)
    private boolean notified7Days = false;

    @Column(name = "notified_3_days", nullable = false)
    private boolean notified3Days = false;

    @Column(name = "notified_expired", nullable = false)
    private boolean notifiedExpired = false;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Subscription() {
    }

    public Subscription(Long id, Long userId, Long planId, LocalDateTime startDate, LocalDateTime expiryDate, SubscriptionStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.planId = planId;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
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

    public boolean isNotified7Days() {
        return notified7Days;
    }

    public void setNotified7Days(boolean notified7Days) {
        this.notified7Days = notified7Days;
    }

    public boolean isNotified3Days() {
        return notified3Days;
    }

    public void setNotified3Days(boolean notified3Days) {
        this.notified3Days = notified3Days;
    }

    public boolean isNotifiedExpired() {
        return notifiedExpired;
    }

    public void setNotifiedExpired(boolean notifiedExpired) {
        this.notifiedExpired = notifiedExpired;
    }
}
