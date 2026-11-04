package com.subtrack.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${internal.gateway-token:}")
    private String internalGatewayToken;

    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'JWT_SECRET' is not configured. API Gateway startup aborted.");
        }
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'JWT_SECRET' must be at least 32 bytes (256 bits) for HMAC-SHA256. API Gateway startup aborted.");
        }
        if (internalGatewayToken == null || internalGatewayToken.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' is not configured. API Gateway startup aborted.");
        }
        if (internalGatewayToken.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: 'INTERNAL_GATEWAY_TOKEN' must be at least 32 bytes (256 bits). API Gateway startup aborted.");
        }
    }

    public void setInternalGatewayToken(String internalGatewayToken) {
        this.internalGatewayToken = internalGatewayToken;
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before default routing filters
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Sanitize inbound headers to prevent spoofing of identity and internal communication tokens
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                    headers.remove("X-User-Email");
                    headers.remove("X-Gateway-Token");
                    headers.remove("X-Internal-Service");
                })
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        String path = sanitizedRequest.getURI().getPath();
        HttpMethod method = sanitizedRequest.getMethod();

        // 2. Allow CORS preflight requests
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(sanitizedExchange);
        }

        // 3. Allow public unauthenticated endpoints
        if (isPublicEndpoint(path, method)) {
            return chain.filter(sanitizedExchange);
        }

        // 4. Block direct external calls to mock payment processing engine
        if (path.equals("/api/payments/process") && HttpMethod.POST.equals(method)) {
            log.warn("Blocked direct external call to payment processor at {}", path);
            return onError(sanitizedExchange, HttpStatus.FORBIDDEN, "Direct external access to payment processing is forbidden. Subscriptions must be activated through the Subscription Service.");
        }

        // 5. Validate Authorization Header presence
        String authHeader = sanitizedRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt to {}: Missing or malformed Authorization header", path);
            return onError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header. Bearer token required.");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return onError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "Bearer token is empty.");
        }

        // 6. Parse and Validate JWT
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired for request to {}: {}", path, e.getMessage());
            return onError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "JWT token has expired. Please log in again.");
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or malformed token for request to {}: {}", path, e.getMessage());
            return onError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "Invalid, malformed, or tampered JWT token signature.");
        } catch (Exception e) {
            log.warn("JWT validation error for request to {}: {}", path, e.getMessage());
            return onError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "JWT authentication failed: " + e.getMessage());
        }

        // 7. Extract Identity Claims
        String role = (String) claims.get("role");
        Object userIdObj = claims.get("userId");
        String userId = userIdObj != null ? String.valueOf(userIdObj) : "";
        String email = claims.getSubject();

        // 8. Role-Based Access Control for Admin-Only Endpoints
        if (isAdminOnlyEndpoint(path, method)) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                log.warn("Forbidden: User {} with role '{}' attempted to access admin endpoint {} [{}]", userId, role, path, method);
                return onError(sanitizedExchange, HttpStatus.FORBIDDEN, "Access denied: Administrator privileges required.");
            }
        }

        // 9. Mutate request with authenticated headers and trusted internal gateway signature
        ServerHttpRequest mutatedRequest = sanitizedExchange.getRequest().mutate()
                .header("X-Gateway-Token", internalGatewayToken)
                .header("X-User-Id", userId)
                .header("X-User-Role", role != null ? role : "USER")
                .header("X-User-Email", email != null ? email : "")
                .build();

        return chain.filter(sanitizedExchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        if (path.equals("/") || path.startsWith("/actuator") || path.startsWith("/eureka")) {
            return true;
        }
        if (path.equals("/api/users/login") && HttpMethod.POST.equals(method)) {
            return true;
        }
        if (path.equals("/api/users/register") && HttpMethod.POST.equals(method)) {
            return true;
        }
        if (path.equals("/api/users/validate") && HttpMethod.GET.equals(method)) {
            return true;
        }
        // Anonymous visitors are allowed to view active plans catalog
        if ((path.equals("/api/plans") || path.equals("/api/plans/")) && HttpMethod.GET.equals(method)) {
            return true;
        }
        return false;
    }

    private boolean isAdminOnlyEndpoint(String path, HttpMethod method) {
        // Plan management operations
        if (path.equals("/api/plans/all")) {
            return true;
        }
        if (path.startsWith("/api/plans") && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.DELETE.equals(method))) {
            return true;
        }
        // Viewing all users subscriptions
        if (path.equals("/api/subscriptions/all") || path.equals("/api/subscriptions")) {
            return true;
        }
        // Viewing all payments
        if (path.equals("/api/payments/all") || path.equals("/api/payments")) {
            return true;
        }
        // Fetching all registered users list
        if (path.equals("/api/users") && HttpMethod.GET.equals(method)) {
            return true;
        }
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                LocalDateTime.now().format(ISO_FORMATTER),
                status.value(),
                status.getReasonPhrase(),
                escapeJson(message)
        );

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
