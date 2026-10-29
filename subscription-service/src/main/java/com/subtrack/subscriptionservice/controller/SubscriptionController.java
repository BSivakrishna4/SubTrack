package com.subtrack.subscriptionservice.controller;

import com.subtrack.subscriptionservice.dto.ActivateRequest;
import com.subtrack.subscriptionservice.dto.RenewRequest;
import com.subtrack.subscriptionservice.dto.SubscriptionResponse;
import com.subtrack.subscriptionservice.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(@Valid @RequestBody ActivateRequest activateRequest) {
        SubscriptionResponse response = subscriptionService.activateSubscription(activateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptionsExplicit() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable("id") Long id) {
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByUserId(@PathVariable("userId") Long userId) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(@PathVariable("id") Long id,
                                                                  @RequestBody(required = false) RenewRequest renewRequest) {
        SubscriptionResponse response = subscriptionService.renewSubscription(id, renewRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable("id") Long id) {
        SubscriptionResponse response = subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable("id") Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
