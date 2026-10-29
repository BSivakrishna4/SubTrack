package com.subtrack.subscriptionservice.service;

import com.subtrack.subscriptionservice.client.PaymentClient;
import com.subtrack.subscriptionservice.client.UserClient;
import com.subtrack.subscriptionservice.dto.*;
import com.subtrack.subscriptionservice.entity.Plan;
import com.subtrack.subscriptionservice.entity.Subscription;
import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import com.subtrack.subscriptionservice.exception.PaymentFailedException;
import com.subtrack.subscriptionservice.exception.ResourceNotFoundException;
import com.subtrack.subscriptionservice.repository.PlanRepository;
import com.subtrack.subscriptionservice.repository.SubscriptionRepository;
import com.subtrack.subscriptionservice.service.EmailService;
import com.subtrack.subscriptionservice.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentClient paymentClient;
    private final UserClient userClient;
    private final EmailService emailService;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   PlanRepository planRepository,
                                   PaymentClient paymentClient,
                                   UserClient userClient,
                                   EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentClient = paymentClient;
        this.userClient = userClient;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public SubscriptionResponse activateSubscription(ActivateRequest activateRequest) {
        Plan plan = planRepository.findById(activateRequest.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + activateRequest.getPlanId()));

        if (!plan.isActive()) {
            throw new IllegalArgumentException("Selected plan is not currently active");
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expiryDate = now.plusDays(plan.getDurationDays());

        // Create pending subscription record
        Subscription subscription = new Subscription();
        subscription.setUserId(activateRequest.getUserId());
        subscription.setPlanId(plan.getId());
        subscription.setStartDate(now);
        subscription.setExpiryDate(expiryDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Process Mock Payment via OpenFeign Client
        PaymentRequest paymentRequest = new PaymentRequest(
                activateRequest.getUserId(),
                savedSubscription.getId(),
                plan.getPrice(),
                activateRequest.isSimulatePaymentFailure()
        );

        PaymentResponse paymentResponse;
        try {
            paymentResponse = paymentClient.processPayment(paymentRequest);
        } catch (Exception e) {
            subscriptionRepository.delete(savedSubscription);
            throw new PaymentFailedException("Payment service unavailable or call failed: " + e.getMessage());
        }

        if (!"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            subscriptionRepository.delete(savedSubscription);
            throw new PaymentFailedException("Payment FAILED. Subscription activation aborted. Transaction Ref: " + paymentResponse.getTransactionReference());
        }

        return mapToSubscriptionResponse(savedSubscription, plan, paymentResponse);
    }

    @Override
    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + id));
        Plan plan = planRepository.findById(subscription.getPlanId()).orElse(null);
        return mapToSubscriptionResponse(subscription, plan, null);
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionsByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(sub -> {
                    Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                    return mapToSubscriptionResponse(sub, plan, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(sub -> {
                    Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                    return mapToSubscriptionResponse(sub, plan, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(Long id, RenewRequest renewRequest) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + id));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found for subscription ID: " + id));

        PaymentRequest paymentRequest = new PaymentRequest(
                subscription.getUserId(),
                subscription.getId(),
                plan.getPrice(),
                renewRequest != null && renewRequest.isSimulatePaymentFailure()
        );

        PaymentResponse paymentResponse = paymentClient.processPayment(paymentRequest);

        if (!"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new PaymentFailedException("Renewal payment FAILED. Subscription was not renewed.");
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime currentExpiry = subscription.getExpiryDate();
        LocalDateTime newExpiry = (currentExpiry != null && currentExpiry.isAfter(now))
                ? currentExpiry.plusDays(plan.getDurationDays())
                : now.plusDays(plan.getDurationDays());

        subscription.setExpiryDate(newExpiry);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setNotified7Days(false);
        subscription.setNotified3Days(false);
        subscription.setNotifiedExpired(false);
        subscription.setUpdatedAt(now);

        Subscription updatedSub = subscriptionRepository.save(subscription);
        return mapToSubscriptionResponse(updatedSub, plan, paymentResponse);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + id));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Subscription savedSub = subscriptionRepository.save(subscription);
        Plan plan = planRepository.findById(savedSub.getPlanId()).orElse(null);
        return mapToSubscriptionResponse(savedSub, plan, null);
    }

    @Override
    @Transactional
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + id));
        subscriptionRepository.delete(subscription);
    }

    @Override
    @Transactional
    public void checkAndExpireSubscriptions() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDate today = LocalDate.now();

        // 1. Existing lifecycle: ACTIVE subscriptions past expiry date transition to EXPIRED
        List<Subscription> expiredSubs = subscriptionRepository.findByStatusAndExpiryDateBefore(SubscriptionStatus.ACTIVE, now);
        for (Subscription sub : expiredSubs) {
            log.info("Subscription #{} has reached or passed expiryDate ({}). Status transitioning: ACTIVE -> EXPIRED", sub.getId(), sub.getExpiryDate());
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setUpdatedAt(now);
            subscriptionRepository.save(sub);
        }

        // 2. Milestone 3: Expiry notification for EXPIRED subscriptions that have not yet been notified
        List<Subscription> pendingExpiredEmailSubs = subscriptionRepository.findByStatusAndNotifiedExpiredFalse(SubscriptionStatus.EXPIRED);
        for (Subscription sub : pendingExpiredEmailSubs) {
            try {
                log.info("Subscription #{} is EXPIRED and notified_expired=false. Fetching user info via UserClient...", sub.getId());
                UserDto user = userClient.getUserById(sub.getUserId());
                Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                String planName = plan != null ? plan.getName() : "SubTrack";
                String userName = user != null ? user.getName() : "User";
                String userEmail = user != null ? user.getEmail() : null;

                log.info("UserClient retrieved profile for subscription #{}: name='{}', email='{}'", sub.getId(), userName, userEmail);

                if (userEmail != null && !userEmail.trim().isEmpty()) {
                    boolean sent = emailService.sendExpiredNotification(userEmail, userName, planName, sub.getExpiryDate(), sub.getId());
                    if (sent) {
                        sub.setNotifiedExpired(true);
                        sub.setUpdatedAt(now);
                        subscriptionRepository.save(sub);
                        log.info("Subscription #{} notification state updated: notified_expired=true saved to database", sub.getId());
                    }
                } else {
                    log.warn("Cannot send expiry email for subscription #{}: user email is null or empty", sub.getId());
                }
            } catch (Exception e) {
                log.error("Email sending failed for subscription ID: {} ({})", sub.getId(), e.getMessage());
            }
        }

        // 3. Milestones 1 & 2: 7-day and 3-day reminder notifications for ACTIVE subscriptions using calendar days
        List<Subscription> activeSubs = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        for (Subscription sub : activeSubs) {
            if (sub.getExpiryDate() == null) continue;

            LocalDate expiryLocalDate = sub.getExpiryDate().toLocalDate();
            long calendarDaysRemaining = ChronoUnit.DAYS.between(today, expiryLocalDate);

            log.info("Evaluating subscription #{}: userId={}, planId={}, expiryDate={}, calendarDaysRemaining={}, notified7Days={}, notified3Days={}",
                    sub.getId(), sub.getUserId(), sub.getPlanId(), sub.getExpiryDate(), calendarDaysRemaining, sub.isNotified7Days(), sub.isNotified3Days());

            // Check 3-day milestone (calendar days remaining between 1 and 3 days, and not yet passed expiry time)
            if (calendarDaysRemaining <= 3 && calendarDaysRemaining > 0 && sub.getExpiryDate().isAfter(now)) {
                if (!sub.isNotified3Days()) {
                    try {
                        log.info("Subscription #{} qualified for 3-day reminder (calendarDaysRemaining={}). Calling UserClient...", sub.getId(), calendarDaysRemaining);
                        UserDto user = userClient.getUserById(sub.getUserId());
                        Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                        String planName = plan != null ? plan.getName() : "SubTrack";
                        String userName = user != null ? user.getName() : "User";
                        String userEmail = user != null ? user.getEmail() : null;

                        log.info("UserClient retrieved profile for subscription #{}: name='{}', email='{}'", sub.getId(), userName, userEmail);

                        if (userEmail != null && !userEmail.trim().isEmpty()) {
                            boolean sent = emailService.send3DaysExpiryNotification(userEmail, userName, planName, sub.getExpiryDate(), sub.getId());
                            if (sent) {
                                sub.setNotified3Days(true);
                                sub.setNotified7Days(true); // Mark 7-day as handled so it doesn't fire an obsolete 7-day email
                                sub.setUpdatedAt(now);
                                subscriptionRepository.save(sub);
                                log.info("Subscription #{} notification state updated: notified_3_days=true saved to database", sub.getId());
                            }
                        } else {
                            log.warn("Cannot send 3-day reminder for subscription #{}: user email is null or empty", sub.getId());
                        }
                    } catch (Exception e) {
                        log.error("Email sending failed for subscription ID: {} ({})", sub.getId(), e.getMessage());
                    }
                }
            }
            // Check 7-day milestone (calendar days remaining between 4 and 7 days, and not yet passed expiry time)
            else if (calendarDaysRemaining <= 7 && calendarDaysRemaining > 3 && sub.getExpiryDate().isAfter(now)) {
                if (!sub.isNotified7Days()) {
                    try {
                        log.info("Subscription #{} qualified for 7-day reminder (calendarDaysRemaining={}). Calling UserClient...", sub.getId(), calendarDaysRemaining);
                        UserDto user = userClient.getUserById(sub.getUserId());
                        Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                        String planName = plan != null ? plan.getName() : "SubTrack";
                        String userName = user != null ? user.getName() : "User";
                        String userEmail = user != null ? user.getEmail() : null;

                        log.info("UserClient retrieved profile for subscription #{}: name='{}', email='{}'", sub.getId(), userName, userEmail);

                        if (userEmail != null && !userEmail.trim().isEmpty()) {
                            boolean sent = emailService.send7DaysExpiryNotification(userEmail, userName, planName, sub.getExpiryDate(), sub.getId());
                            if (sent) {
                                sub.setNotified7Days(true);
                                sub.setUpdatedAt(now);
                                subscriptionRepository.save(sub);
                                log.info("Subscription #{} notification state updated: notified_7_days=true saved to database", sub.getId());
                            }
                        } else {
                            log.warn("Cannot send 7-day reminder for subscription #{}: user email is null or empty", sub.getId());
                        }
                    } catch (Exception e) {
                        log.error("Email sending failed for subscription ID: {} ({})", sub.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription, Plan plan, PaymentResponse paymentResponse) {
        PlanDto planDto = null;
        if (plan != null) {
            planDto = new PlanDto(
                    plan.getId(),
                    plan.getName(),
                    plan.getDescription(),
                    plan.getPrice(),
                    plan.getDurationDays(),
                    plan.isActive()
            );
        }

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId(),
                planDto,
                subscription.getStartDate(),
                subscription.getExpiryDate(),
                subscription.getStatus(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt(),
                paymentResponse
        );
    }
}
