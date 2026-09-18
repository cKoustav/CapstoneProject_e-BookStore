package com.ebookstore.service;

import com.ebookstore.dto.AuthResponse;
import com.ebookstore.dto.LoginRequest;
import com.ebookstore.dto.RegisterRequest;
import com.ebookstore.model.User;
import com.ebookstore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    // Simple in-memory token store for session management
    private final Map<String, String> tokenToUserIdMap = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                UUID.randomUUID().toString(),
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                hashPassword(request.getPassword()),
                request.getFullName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : "",
                LocalDateTime.now()
        );

        userRepository.save(user);

        String token = generateToken(user.getId());
        return AuthResponse.success(token, toUserResponse(user), "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier.toLowerCase());
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        User user = userOpt.get();
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        String token = generateToken(user.getId());
        return AuthResponse.success(token, toUserResponse(user), "Login successful");
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        // Handle "Bearer <token>" format if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        String userId = tokenToUserIdMap.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public void logout(String token) {
        if (token != null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            tokenToUserIdMap.remove(token);
        }
    }

    private String generateToken(String userId) {
        String token = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        tokenToUserIdMap.put(token, userId);
        return token;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equalsIgnoreCase(hashedPassword);
    }

    private AuthResponse.UserResponse toUserResponse(User user) {
        return new AuthResponse.UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }
}
