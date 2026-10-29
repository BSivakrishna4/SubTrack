package com.subtrack.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long paymentId;
    private String transactionReference;
    private String status;
    private BigDecimal amount;
    private Long userId;
    private Long subscriptionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    public PaymentResponse() {
    }

    public PaymentResponse(Long paymentId, String transactionReference, String status, BigDecimal amount, Long userId, Long subscriptionId, LocalDateTime paymentDate) {
        this.paymentId = paymentId;
        this.transactionReference = transactionReference;
        this.status = status;
        this.amount = amount;
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.paymentDate = paymentDate;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}
