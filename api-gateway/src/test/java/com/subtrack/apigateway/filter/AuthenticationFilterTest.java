package com.subtrack.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private GatewayFilterChain chain;
    private final String secret = "TestSecretKeyForJWTSecurityAuthenticationVerification2026SecureKey!";
    private final String gatewayToken = "TestInternalGatewayTokenForVerification2026SecureKey!";

    @BeforeEach
    void setUp() {
        filter = new AuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", secret);
        ReflectionTestUtils.setField(filter, "internalGatewayToken", gatewayToken);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    private String createTestToken(Long userId, String email, String role, long expirationMs) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Fail-fast Validation: Missing JWT_SECRET throws IllegalStateException")
    void testValidateSecret_Missing_ThrowsException() {
        AuthenticationFilter f = new AuthenticationFilter();
        ReflectionTestUtils.setField(f, "jwtSecret", "");
        ReflectionTestUtils.setField(f, "internalGatewayToken", gatewayToken);
        assertThrows(IllegalStateException.class, f::validateSecret);
    }

    @Test
    @DisplayName("Fail-fast Validation: Short JWT_SECRET (<32 bytes) throws IllegalStateException")
    void testValidateSecret_Short_ThrowsException() {
        AuthenticationFilter f = new AuthenticationFilter();
        ReflectionTestUtils.setField(f, "jwtSecret", "short-secret");
        ReflectionTestUtils.setField(f, "internalGatewayToken", gatewayToken);
        assertThrows(IllegalStateException.class, f::validateSecret);
    }

    @Test
    @DisplayName("Fail-fast Validation: Missing INTERNAL_GATEWAY_TOKEN throws IllegalStateException")
    void testValidateGatewayToken_Missing_ThrowsException() {
        AuthenticationFilter f = new AuthenticationFilter();
        ReflectionTestUtils.setField(f, "jwtSecret", secret);
        ReflectionTestUtils.setField(f, "internalGatewayToken", "");
        assertThrows(IllegalStateException.class, f::validateSecret);
    }

    @Test
    @DisplayName("Fail-fast Validation: Short INTERNAL_GATEWAY_TOKEN (<32 bytes) throws IllegalStateException")
    void testValidateGatewayToken_Short_ThrowsException() {
        AuthenticationFilter f = new AuthenticationFilter();
        ReflectionTestUtils.setField(f, "jwtSecret", secret);
        ReflectionTestUtils.setField(f, "internalGatewayToken", "short-gateway-token");
        assertThrows(IllegalStateException.class, f::validateSecret);
    }

    @Test
    @DisplayName("Fail-fast Validation: Valid credentials pass startup check")
    void testValidateCredentials_Valid_Passes() {
        AuthenticationFilter f = new AuthenticationFilter();
        ReflectionTestUtils.setField(f, "jwtSecret", secret);
        ReflectionTestUtils.setField(f, "internalGatewayToken", gatewayToken);
        f.validateSecret();
    }

    @Test
    @DisplayName("Inbound Header Sanitization: Spoofed client identity headers are stripped")
    void testInboundHeaderSanitization_SpoofedHeadersStripped() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/users/login")
                .header("X-User-Role", "ADMIN")
                .header("X-User-Id", "999")
                .header("X-Gateway-Token", "fake-token")
                .header("X-Internal-Service", "fake-secret")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain, times(1)).filter(captor.capture());

        ServerWebExchange forwarded = captor.getValue();
        assertNull(forwarded.getRequest().getHeaders().getFirst("X-User-Role"));
        assertNull(forwarded.getRequest().getHeaders().getFirst("X-User-Id"));
        assertNull(forwarded.getRequest().getHeaders().getFirst("X-Gateway-Token"));
        assertNull(forwarded.getRequest().getHeaders().getFirst("X-Internal-Service"));
    }

    @Test
    @DisplayName("Public Endpoint: POST /api/users/login should pass through without token")
    void testPublicEndpoint_PassesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/users/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected Endpoint: Missing token should return 401 Unauthorized")
    void testProtectedEndpoint_MissingToken_Returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/subscriptions/user/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected Endpoint: Malformed token should return 401 Unauthorized")
    void testProtectedEndpoint_MalformedToken_Returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/subscriptions/user/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.malformed.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected Endpoint: Expired token should return 401 Unauthorized")
    void testProtectedEndpoint_ExpiredToken_Returns401() {
        String expiredToken = createTestToken(1L, "user@subtrack.com", "USER", -10000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/subscriptions/user/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Admin Endpoint: Regular USER accessing admin endpoint should return 403 Forbidden")
    void testAdminEndpoint_RegularUser_Returns403() {
        String userToken = createTestToken(1L, "user@subtrack.com", "USER", 3600000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/plans/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Admin Endpoint: ADMIN role passes through and injects X-Gateway-Token and identity headers")
    void testAdminEndpoint_AdminUser_PassesThrough() {
        String adminToken = createTestToken(99L, "admin@subtrack.com", "ADMIN", 3600000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/plans/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain, times(1)).filter(captor.capture());

        ServerWebExchange forwarded = captor.getValue();
        assertEquals(gatewayToken, forwarded.getRequest().getHeaders().getFirst("X-Gateway-Token"));
        assertEquals("99", forwarded.getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("ADMIN", forwarded.getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    @DisplayName("Direct External Payment Process: Calling /api/payments/process externally returns 403")
    void testDirectPaymentCall_Returns403() {
        String userToken = createTestToken(1L, "user@subtrack.com", "USER", 3600000);
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/payments/process")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }
}
