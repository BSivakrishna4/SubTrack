package com.subtrack.paymentservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(name = "payment_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime paymentDate;

    public Payment() {
    }

    public Payment(Long id, Long userId, Long subscriptionId, BigDecimal amount, String paymentStatus, String transactionReference, LocalDateTime paymentDate) {
        this.id = id;
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.paymentDate = paymentDate;
    }

    @PrePersist
    protected void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        }
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}
