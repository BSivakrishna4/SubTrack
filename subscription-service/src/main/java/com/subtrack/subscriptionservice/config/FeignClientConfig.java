package com.subtrack.subscriptionservice.config;

import feign.RequestInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class FeignClientConfig {

    @Value("${internal.service-secret:}")
    private String internalServiceSecret;

    @PostConstruct
    public void validateSecret() {
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' is not configured. FeignClient startup aborted.");
        }
        if (internalServiceSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' must be at least 32 bytes (256 bits). FeignClient startup aborted.");
        }
    }

    public void setInternalServiceSecret(String internalServiceSecret) {
        this.internalServiceSecret = internalServiceSecret;
    }

    @Bean
    public RequestInterceptor internalServiceRequestInterceptor() {
        return requestTemplate -> {
            if (internalServiceSecret != null && !internalServiceSecret.trim().isEmpty()) {
                requestTemplate.header("X-Internal-Service", internalServiceSecret.trim());
            }
        };
    }
}
