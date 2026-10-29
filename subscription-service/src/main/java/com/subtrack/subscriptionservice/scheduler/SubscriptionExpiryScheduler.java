package com.subtrack.subscriptionservice.scheduler;

import com.subtrack.subscriptionservice.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryScheduler.class);
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionExpiryScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(fixedRateString = "${subscription.expiry-check-rate-ms:30000}")
    public void runExpiryCheck() {
        log.debug("Running automated subscription expiry detection task...");
        subscriptionService.checkAndExpireSubscriptions();
    }
}
