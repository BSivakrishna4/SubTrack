package com.subtrack.userservice.service;

import com.subtrack.userservice.dto.AuthResponse;
import com.subtrack.userservice.dto.LoginRequest;
import com.subtrack.userservice.dto.RegisterRequest;
import com.subtrack.userservice.dto.UserResponse;
import com.subtrack.userservice.entity.User;
import com.subtrack.userservice.exception.InvalidCredentialsException;
import com.subtrack.userservice.exception.ResourceNotFoundException;
import com.subtrack.userservice.exception.UserAlreadyExistsException;
import com.subtrack.userservice.repository.UserRepository;
import com.subtrack.userservice.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void seedInitialUsers() {
        if (!userRepository.existsByEmail("admin@subtrack.com")) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@subtrack.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            userRepository.save(admin);
        }
        if (!userRepository.existsByEmail("user@subtrack.com")) {
            User demoUser = new User();
            demoUser.setName("Demo User");
            demoUser.setEmail("user@subtrack.com");
            demoUser.setPassword(passwordEncoder.encode("user123"));
            demoUser.setRole("USER");
            demoUser.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            userRepository.save(demoUser);
        }
    }

    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email '" + registerRequest.getEmail() + "' already exists");
        }

        User user = new User();
        user.setName(registerRequest.getName().trim());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        String role = (registerRequest.getRole() != null && !registerRequest.getRole().trim().isEmpty())
                ? registerRequest.getRole().toUpperCase()
                : "USER";
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
