package com.subtrack.subscriptionservice.dto;

public class RenewRequest {

    private boolean simulatePaymentFailure = false;

    public RenewRequest() {
    }

    public RenewRequest(boolean simulatePaymentFailure) {
        this.simulatePaymentFailure = simulatePaymentFailure;
    }

    public boolean isSimulatePaymentFailure() {
        return simulatePaymentFailure;
    }

    public void setSimulatePaymentFailure(boolean simulatePaymentFailure) {
        this.simulatePaymentFailure = simulatePaymentFailure;
    }
}
