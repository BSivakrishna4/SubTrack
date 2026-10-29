package com.subtrack.subscriptionservice.service;

import java.time.LocalDateTime;

public interface EmailService {

    boolean send7DaysExpiryNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId);

    boolean send3DaysExpiryNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId);

    boolean sendExpiredNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId);
}
