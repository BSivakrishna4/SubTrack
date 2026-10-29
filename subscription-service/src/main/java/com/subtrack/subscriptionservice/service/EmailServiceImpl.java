package com.subtrack.subscriptionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.enabled:${MAIL_ENABLED:true}}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean send7DaysExpiryNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId) {
        String subject = "SubTrack Subscription Expires in 7 Days";
        String formattedDate = expiryDate != null ? expiryDate.format(DATE_FORMATTER) : "N/A";

        String body = "Hello " + (userName != null ? userName : "User") + ",\n\n"
                + "Your " + (planName != null ? planName : "SubTrack") + " subscription will expire in 7 days.\n\n"
                + "Expiry Date: " + formattedDate + "\n\n"
                + "Please renew your subscription before it expires.\n\n"
                + "Regards,\n"
                + "SubTrack Team";

        boolean sent = sendEmail(toEmail, subject, body, subscriptionId);
        if (sent) {
            log.info("7-day expiry notification sent to {}", toEmail);
        }
        return sent;
    }

    @Override
    public boolean send3DaysExpiryNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId) {
        String subject = "SubTrack Subscription Expires in 3 Days";
        String formattedDate = expiryDate != null ? expiryDate.format(DATE_FORMATTER) : "N/A";

        String body = "Hello " + (userName != null ? userName : "User") + ",\n\n"
                + "Your " + (planName != null ? planName : "SubTrack") + " subscription will expire in 3 days.\n\n"
                + "Expiry Date: " + formattedDate + "\n\n"
                + "Please renew your subscription to continue using the service.\n\n"
                + "Regards,\n"
                + "SubTrack Team";

        boolean sent = sendEmail(toEmail, subject, body, subscriptionId);
        if (sent) {
            log.info("3-day expiry notification sent to {}", toEmail);
        }
        return sent;
    }

    @Override
    public boolean sendExpiredNotification(String toEmail, String userName, String planName, LocalDateTime expiryDate, Long subscriptionId) {
        String subject = "Your SubTrack Subscription Has Expired";
        String formattedDate = expiryDate != null ? expiryDate.format(DATE_FORMATTER) : "N/A";

        String body = "Hello " + (userName != null ? userName : "User") + ",\n\n"
                + "Your " + (planName != null ? planName : "SubTrack") + " subscription has expired.\n\n"
                + "Expiry Date: " + formattedDate + "\n\n"
                + "Please renew your subscription to continue using the service.\n\n"
                + "Regards,\n"
                + "SubTrack Team";

        boolean sent = sendEmail(toEmail, subject, body, subscriptionId);
        if (sent) {
            log.info("Expiry notification sent to {}", toEmail);
        }
        return sent;
    }

    private boolean sendEmail(String toEmail, String subject, String body, Long subscriptionId) {
        if (!mailEnabled) {
            log.warn("Email sending is disabled (MAIL_ENABLED is false). Skipping email notification for subscription ID: {}", subscriptionId);
            return false;
        }

        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Cannot send email for subscription ID {}: recipient email address is missing.", subscriptionId);
            return false;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.warn("SMTP username is not configured (SMTP_USERNAME is empty). Email sending failed for subscription ID: {}", subscriptionId);
            return false;
        }

        try {
            log.info("Initiating email dispatch via JavaMailSender: to='{}', from='{}', subject='{}', subscriptionId={}", toEmail, fromEmail, subject, subscriptionId);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail.trim());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email delivered to SMTP server successfully for subscription ID {}: to='{}'", subscriptionId, toEmail);
            return true;
        } catch (Exception e) {
            log.error("SMTP error while sending email for subscription ID {}: to='{}', error='{}'", subscriptionId, toEmail, e.getMessage());
            return false;
        }
    }
}
