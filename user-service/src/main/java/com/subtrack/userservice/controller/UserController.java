package com.subtrack.userservice.controller;

import com.subtrack.userservice.dto.AuthResponse;
import com.subtrack.userservice.dto.LoginRequest;
import com.subtrack.userservice.dto.RegisterRequest;
import com.subtrack.userservice.dto.UserResponse;
import com.subtrack.userservice.security.JwtUtil;
import com.subtrack.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
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

        String token = authHeader.substring(7);
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
