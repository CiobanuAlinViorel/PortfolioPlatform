package com.example.portfolio.auth.application;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.ForgotPasswordRequest;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.RegisterRequest;
import com.example.portfolio.auth.dto.ResetPasswordRequest;
import com.example.portfolio.auth.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${spring.security.jwt.refresh-token-expiration-days:7}")
    private int refreshTokenExpirationDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String verificationToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusDays(1))
                .refreshToken(refreshToken)
                .refreshTokenExpiry(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .build();

        User saved = userRepository.save(user);
        emailService.sendVerificationEmail(saved.getEmail(), verificationToken);

        return AuthResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .token(jwtService.generateToken(saved))
                .emailVerified(saved.isEmailVerified())
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        userRepository.save(user);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtService.generateToken(user))
                .emailVerified(user.isEmailVerified())
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (user.getVerificationTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return without error to avoid leaking whether the email exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refreshTokens(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getRefreshTokenExpiry())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        String newRefreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        userRepository.save(user);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtService.generateToken(user))
                .emailVerified(user.isEmailVerified())
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
        });
    }
}
