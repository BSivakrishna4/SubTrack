package com.subtrack.subscriptionservice.service;

import com.subtrack.subscriptionservice.dto.ActivateRequest;
import com.subtrack.subscriptionservice.dto.RenewRequest;
import com.subtrack.subscriptionservice.dto.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse activateSubscription(ActivateRequest activateRequest);
    SubscriptionResponse getSubscriptionById(Long id);
    List<SubscriptionResponse> getSubscriptionsByUserId(Long userId);
    List<SubscriptionResponse> getAllSubscriptions();
    SubscriptionResponse renewSubscription(Long id, RenewRequest renewRequest);
    SubscriptionResponse cancelSubscription(Long id);
    void deleteSubscription(Long id);
    void checkAndExpireSubscriptions();
}
