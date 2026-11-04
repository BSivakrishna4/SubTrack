package com.subtrack.userservice.controller;

import com.subtrack.userservice.dto.AuthResponse;
import com.subtrack.userservice.dto.LoginRequest;
import com.subtrack.userservice.dto.RegisterRequest;
import com.subtrack.userservice.dto.UserResponse;
import com.subtrack.userservice.exception.AccessDeniedException;
import com.subtrack.userservice.security.JwtUtil;
import com.subtrack.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${internal.gateway-token:}")
    private String internalGatewayToken;

    @Value("${internal.service-secret:}")
    private String internalServiceSecret;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @jakarta.annotation.PostConstruct
    public void validateCredentials() {
        if (internalGatewayToken == null || internalGatewayToken.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' is not configured. User Service startup aborted.");
        }
        if (internalGatewayToken.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' must be at least 32 bytes (256 bits). User Service startup aborted.");
        }
        if (internalServiceSecret == null || internalServiceSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' is not configured. User Service startup aborted.");
        }
        if (internalServiceSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_SERVICE_SECRET' must be at least 32 bytes (256 bits). User Service startup aborted.");
        }
    }

    public void setSecurityTokens(String internalGatewayToken, String internalServiceSecret) {
        this.internalGatewayToken = internalGatewayToken;
        this.internalServiceSecret = internalServiceSecret;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = userService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = userService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        boolean isAuthorizedAdmin = false;

        // 1. Authorized via verified Gateway token and ADMIN role
        if (internalGatewayToken != null && !internalGatewayToken.trim().isEmpty() && internalGatewayToken.equals(gatewayToken)) {
            if ("ADMIN".equalsIgnoreCase(authRole)) {
                isAuthorizedAdmin = true;
            }
        }

        // 2. Authorized via direct Bearer JWT token with ADMIN role
        if (!isAuthorizedAdmin && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtUtil.validateToken(token)) {
                isAuthorizedAdmin = "ADMIN".equalsIgnoreCase(jwtUtil.extractRole(token));
            }
        }

        if (!isAuthorizedAdmin) {
            throw new AccessDeniedException("Access denied: Administrator privileges required to view all user records");
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String authUserId,
            @RequestHeader(value = "X-User-Role", required = false) String authRole,
            @RequestHeader(value = "X-Gateway-Token", required = false) String gatewayToken,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalServiceHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        boolean isAuthorized = false;

        // 1. Legitimate internal OpenFeign call (e.g., Subscription Service email notification scheduler)
        if (internalServiceSecret != null && !internalServiceSecret.trim().isEmpty() && internalServiceSecret.equals(internalServiceHeader)) {
            isAuthorized = true;
        }

        // 2. Gateway-forwarded request with verified token and matching identity/role
        if (!isAuthorized && internalGatewayToken != null && !internalGatewayToken.trim().isEmpty() && internalGatewayToken.equals(gatewayToken)) {
            if ("ADMIN".equalsIgnoreCase(authRole) || (authUserId != null && authUserId.trim().equals(String.valueOf(id)))) {
                isAuthorized = true;
            }
        }

        // 3. Direct request with valid Bearer JWT token
        if (!isAuthorized && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtUtil.validateToken(token)) {
                Long tokenUserId = jwtUtil.extractUserId(token);
                String tokenRole = jwtUtil.extractRole(token);
                if ("ADMIN".equalsIgnoreCase(tokenRole) || (tokenUserId != null && tokenUserId.equals(id))) {
                    isAuthorized = true;
                }
            }
        }

        // Fail-closed: Reject unauthenticated headerless requests and unauthorized access
        if (!isAuthorized) {
            throw new AccessDeniedException("Access denied: You are not authorized to view this user profile");
        }

        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("valid", false);
            result.put("message", "Missing or malformed Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        String token = authHeader.substring(7).trim();
        boolean isValid = jwtUtil.validateToken(token);
        result.put("valid", isValid);

        if (isValid) {
            result.put("userId", jwtUtil.extractUserId(token));
            result.put("email", jwtUtil.extractEmail(token));
            result.put("role", jwtUtil.extractRole(token));
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Invalid or expired JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }
}
