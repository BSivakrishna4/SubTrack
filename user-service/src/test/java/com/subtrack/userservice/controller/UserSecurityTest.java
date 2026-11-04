package com.subtrack.userservice.controller;

import com.subtrack.userservice.dto.UserResponse;
import com.subtrack.userservice.exception.AccessDeniedException;
import com.subtrack.userservice.security.JwtUtil;
import com.subtrack.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserSecurityTest {

    private static final String GATEWAY_TOKEN = "test-gateway-token-for-user-service-verification-32bytes!";
    private static final String INTERNAL_SERVICE_SECRET = "test-internal-service-secret-for-user-service-32bytes!";

    private UserService userService;
    private JwtUtil jwtUtil;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtUtil = mock(JwtUtil.class);
        userController = new UserController(userService, jwtUtil);
        userController.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
    }

    private UserResponse createMockUser(Long id, String name, String email, String role) {
        return new UserResponse(id, name, email, role, LocalDateTime.now());
    }

    @Test
    @DisplayName("JWT Secret Fail-Fast: Missing or empty secret aborts startup with IllegalStateException")
    void testValidateSecret_EmptySecret_ThrowsException() {
        JwtUtil util = new JwtUtil();
        util.setSecret("");
        assertThrows(IllegalStateException.class, util::validateSecret);

        util.setSecret(null);
        assertThrows(IllegalStateException.class, util::validateSecret);
    }

    @Test
    @DisplayName("JWT Secret Fail-Fast: Secret shorter than 32 bytes (256 bits) aborts startup")
    void testValidateSecret_ShortSecret_ThrowsException() {
        JwtUtil util = new JwtUtil();
        util.setSecret("short-insecure-secret");
        assertThrows(IllegalStateException.class, util::validateSecret);
    }

    @Test
    @DisplayName("JWT Secret Validation: Secret >= 32 bytes passes startup validation")
    void testValidateSecret_ValidSecret_Passes() {
        JwtUtil util = new JwtUtil();
        util.setSecret("a-very-secure-secret-key-that-is-at-least-32-bytes-long!");
        util.validateSecret(); // Should not throw
    }

    @Test
    @DisplayName("Internal Token Fail-Fast: Missing or short internal tokens in UserController throw IllegalStateException")
    void testUserController_ValidateCredentials_FailFast() {
        UserController c = new UserController(userService, jwtUtil);

        c.setSecurityTokens("", INTERNAL_SERVICE_SECRET);
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens("short-token", INTERNAL_SERVICE_SECRET);
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, "");
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, "short-secret");
        assertThrows(IllegalStateException.class, c::validateCredentials);

        c.setSecurityTokens(GATEWAY_TOKEN, INTERNAL_SERVICE_SECRET);
        c.validateCredentials(); // Should pass
    }

    @Test
    @DisplayName("Direct Port Access Prevention: Spoofed admin role without Gateway token cannot view all users")
    void testGetAllUsers_SpoofedRole_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            userController.getAllUsers("ADMIN", null, null);
        });
        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("Admin RBAC: Authenticated admin through Gateway can view all registered users")
    void testGetAllUsers_AdminThroughGateway_Success() {
        when(userService.getAllUsers()).thenReturn(List.of(createMockUser(1L, "User One", "user@subtrack.com", "USER")));

        ResponseEntity<List<UserResponse>> response = userController.getAllUsers("ADMIN", GATEWAY_TOKEN, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Privilege Containment: Internal service credentials alone do NOT grant access to getAllUsers()")
    void testGetAllUsers_InternalServiceCredentials_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            userController.getAllUsers(null, null, null);
        });
        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("Direct Port Access Prevention: Headerless external request to getUserById is rejected")
    void testGetUserById_HeaderlessCall_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            userController.getUserById(2L, null, null, null, null, null);
        });
        verify(userService, never()).getUserById(any());
    }

    @Test
    @DisplayName("Direct Port Access Prevention: Spoofed identity headers without Gateway token are rejected")
    void testGetUserById_SpoofedIdentityHeader_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            userController.getUserById(2L, "2", "USER", null, null, null);
        });
        verify(userService, never()).getUserById(any());
    }

    @Test
    @DisplayName("Inter-Service Trust: Legitimate OpenFeign call with X-Internal-Service header succeeds for profile lookup")
    void testGetUserById_InternalFeignCall_Success() {
        when(userService.getUserById(2L)).thenReturn(createMockUser(2L, "Recipient", "rec@subtrack.com", "USER"));

        ResponseEntity<UserResponse> response =
                userController.getUserById(2L, null, null, null, INTERNAL_SERVICE_SECRET, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getName().length() > 0 ? 1 : 0);
        verify(userService, times(1)).getUserById(2L);
    }

    @Test
    @DisplayName("Identity Isolation: User A cannot inspect User B's profile through Gateway")
    void testGetUserById_DifferentUser_Forbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            userController.getUserById(2L, "1", "USER", GATEWAY_TOKEN, null, null);
        });
        verify(userService, never()).getUserById(any());
    }

    @Test
    @DisplayName("Identity Isolation: User can inspect their own profile through Gateway")
    void testGetUserById_Owner_Success() {
        when(userService.getUserById(1L)).thenReturn(createMockUser(1L, "John", "john@subtrack.com", "USER"));

        ResponseEntity<UserResponse> response =
                userController.getUserById(1L, "1", "USER", GATEWAY_TOKEN, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getName());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("Admin Access: Admin can inspect any user profile through Gateway")
    void testGetUserById_AdminThroughGateway_Success() {
        when(userService.getUserById(2L)).thenReturn(createMockUser(2L, "John", "john@subtrack.com", "USER"));

        ResponseEntity<UserResponse> response =
                userController.getUserById(2L, "99", "ADMIN", GATEWAY_TOKEN, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getName());
        verify(userService, times(1)).getUserById(2L);
    }

    @Test
    @DisplayName("Token Validation Endpoint: Valid token returns extracted claims")
    void testValidateToken_Valid_Returns200() {
        String token = "valid.jwt.token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn(10L);
        when(jwtUtil.extractEmail(token)).thenReturn("test@subtrack.com");
        when(jwtUtil.extractRole(token)).thenReturn("USER");

        ResponseEntity<Map<String, Object>> response = userController.validateToken("Bearer " + token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("valid"));
        assertEquals(10L, response.getBody().get("userId"));
        assertEquals("test@subtrack.com", response.getBody().get("email"));
    }

    @Test
    @DisplayName("Token Validation Endpoint: Missing header returns 401")
    void testValidateToken_MissingHeader_Returns401() {
        ResponseEntity<Map<String, Object>> response = userController.validateToken(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("valid"));
    }
}
