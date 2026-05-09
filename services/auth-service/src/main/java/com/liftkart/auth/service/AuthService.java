package com.liftkart.auth.service;

import com.liftkart.auth.dto.request.LoginRequest;
import com.liftkart.auth.dto.request.RegisterRequest;
import com.liftkart.auth.dto.request.VendorRegisterRequest;
import com.liftkart.auth.dto.response.AuthResponse;
import com.liftkart.auth.dto.response.UserResponse;
import com.liftkart.auth.entity.RefreshToken;
import com.liftkart.auth.entity.User;
import com.liftkart.auth.entity.VendorProfile;
import com.liftkart.auth.exception.BadRequestException;
import com.liftkart.auth.exception.UnauthorizedException;
import com.liftkart.auth.repository.RefreshTokenRepository;
import com.liftkart.auth.repository.UserRepository;
import com.liftkart.auth.repository.VendorProfileRepository;
import com.liftkart.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Register Customer ──────────────────────────────────────────
    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        validateEmailNotTaken(request.getEmail());

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .role("CUSTOMER")
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("Customer registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    // ── Register Vendor ────────────────────────────────────────────
    @Transactional
    public UserResponse registerVendor(VendorRegisterRequest request) {
        validateEmailNotTaken(request.getEmail());

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .role("VENDOR")
                .isActive(false)   // inactive until admin approves
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);

        VendorProfile profile = VendorProfile.builder()
                .user(user)
                .storeName(request.getStoreName().trim())
                .storeDescription(request.getStoreDescription())
                .approvalStatus("PENDING")
                .build();

        vendorProfileRepository.save(profile);
        log.info("Vendor registration submitted: {}", user.getEmail());

        // Return user details — no token yet (admin must approve first)
        return mapToUserResponse(user);
    }

    // ── Login ──────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailAndIsDeletedFalse(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException(
                    "Your account is not active. " +
                            "If you are a vendor, please wait for admin approval.");
        }

        // Revoke old tokens before issuing new ones
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Get Current User ───────────────────────────────────────────
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return mapToUserResponse(user);
    }

    // ── Logout ─────────────────────────────────────────────────────
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("User logged out: {}", userId);
    }

    // ── Private Helpers ────────────────────────────────────────────

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new BadRequestException("An account with this email already exists");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());

        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .build();
        refreshToken.setCreatedBy(user.getId());

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }
}