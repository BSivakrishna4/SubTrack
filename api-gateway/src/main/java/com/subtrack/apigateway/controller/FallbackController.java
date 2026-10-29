package com.subtrack.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootEndpoint() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "SubTrack API Gateway");
        info.put("status", "UP");
        info.put("port", 8080);
        info.put("frontendUrl", "http://localhost:5173");
        info.put("eurekaDashboard", "http://localhost:8761");
        info.put("proxiedRoutes", new String[]{
            "/api/users/** -> USER-SERVICE (Port 8081)",
            "/api/plans/** -> SUBSCRIPTION-SERVICE (Port 8082)",
            "/api/subscriptions/** -> SUBSCRIPTION-SERVICE (Port 8082)",
            "/api/payments/** -> PAYMENT-SERVICE (Port 8083)"
        });
        return ResponseEntity.ok(info);
    }
}
