package com.subtrack.userservice.service;

import com.subtrack.userservice.dto.AuthResponse;
import com.subtrack.userservice.dto.LoginRequest;
import com.subtrack.userservice.dto.RegisterRequest;
import com.subtrack.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse registerUser(RegisterRequest registerRequest);
    AuthResponse loginUser(LoginRequest loginRequest);
    UserResponse getUserById(Long userId);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsers();
}
