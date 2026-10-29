package com.subtrack.subscriptionservice.repository;

import com.subtrack.subscriptionservice.entity.Subscription;
import com.subtrack.subscriptionservice.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    Optional<Subscription> findFirstByUserIdAndStatusOrderByExpiryDateDesc(Long userId, SubscriptionStatus status);
    List<Subscription> findByStatusAndExpiryDateBefore(SubscriptionStatus status, LocalDateTime dateTime);
    List<Subscription> findByStatus(SubscriptionStatus status);
    List<Subscription> findByStatusAndNotifiedExpiredFalse(SubscriptionStatus status);
    boolean existsByPlanId(Long planId);
}
