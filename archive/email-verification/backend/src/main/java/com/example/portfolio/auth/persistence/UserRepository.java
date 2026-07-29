package com.example.portfolio.auth.persistence;

import com.example.portfolio.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    Optional<User> findByRefreshToken(String token);
    Optional<User> findByGoogleId(String googleId);
    long deleteByEmailVerifiedFalseAndVerificationTokenExpiryBefore(LocalDateTime cutoff);
}
