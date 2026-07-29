package com.example.portfolio.auth.application;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.dto.*;
import com.example.portfolio.auth.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationDays", 7);
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenEmailDoesNotExist() {
        RegisterRequest request = registerRequest("user@test.com", "raw-password");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse result = authService.register(request);

        assertThat(result.email()).isEqualTo("user@test.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.isEmailVerified()).isTrue();

        verifyNoInteractions(emailService);
    }

    @Test
    void register_shouldSetFutureExpiryForRefreshToken() {
        RegisterRequest request = registerRequest("user@test.com", "raw-password");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRefreshTokenExpiry()).isAfter(LocalDateTime.now());
        assertThat(savedUser.getRefreshToken()).isNotBlank();
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = registerRequest("user@test.com", "raw-password");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail("user@test.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(emailService);
    }

    // ── authenticate ─────────────────────────────────────────────────────────

    @Test
    void authenticate_shouldReturnAuthResponse_whenCredentialsAreValid() {
        LoginRequest request = loginRequest("user@test.com", "raw-password");

        User user = User.builder()
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse result = authService.authenticate(request);

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getRefreshToken()).isNotBlank();
    }

    @Test
    void authenticate_shouldSaveUserWithNewRefreshTokenAndFutureExpiry() {
        LoginRequest request = loginRequest("user@test.com", "raw-password");

        User user = User.builder()
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        authService.authenticate(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRefreshToken()).isNotBlank();
        assertThat(savedUser.getRefreshTokenExpiry()).isAfter(LocalDateTime.now());
    }

    @Test
    void authenticate_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequest request = loginRequest("missing@test.com", "raw-password");

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("missing@test.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordIsWrong() {
        LoginRequest request = loginRequest("user@test.com", "wrong-password");

        User user = User.builder()
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    // ── forgotPassword ───────────────────────────────────────────────────────

    @Test
    void forgotPassword_shouldSetResetTokenAndSendEmail_whenEmailExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@test.com");

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        authService.forgotPassword(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPasswordResetToken()).isNotBlank();
        assertThat(savedUser.getPasswordResetTokenExpiry()).isAfter(LocalDateTime.now());

        verify(emailService).sendPasswordResetEmail(eq("user@test.com"), anyString());
    }

    @Test
    void forgotPassword_shouldDoNothing_whenEmailDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("missing@test.com");

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verify(userRepository).findByEmail("missing@test.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    // ── resetPassword ────────────────────────────────────────────────────────

    @Test
    void resetPassword_shouldUpdatePassword_whenTokenIsValidAndNotExpired() {
        String resetToken = "valid-reset-token";
        ResetPasswordRequest request = new ResetPasswordRequest(resetToken, "new-password");

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .passwordResetToken(resetToken)
                .passwordResetTokenExpiry(LocalDateTime.now().plusMinutes(30))
                .build();

        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo("encoded-new-password");
        assertThat(savedUser.getPasswordResetToken()).isNull();
        assertThat(savedUser.getPasswordResetTokenExpiry()).isNull();
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest("unknown-token", "new-password");

        when(userRepository.findByPasswordResetToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired reset token");

        verify(userRepository).findByPasswordResetToken("unknown-token");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenIsExpired() {
        String resetToken = "expired-reset-token";
        ResetPasswordRequest request = new ResetPasswordRequest(resetToken, "new-password");

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .passwordResetToken(resetToken)
                .passwordResetTokenExpiry(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reset token has expired");

        verify(userRepository).findByPasswordResetToken(resetToken);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenExpiryIsNull() {
        String resetToken = "token-without-expiry";
        ResetPasswordRequest request = new ResetPasswordRequest(resetToken, "new-password");

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .passwordResetToken(resetToken)
                .passwordResetTokenExpiry(null)
                .build();

        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reset token has expired");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // ── refreshTokens ────────────────────────────────────────────────────────

    @Test
    void refreshTokens_shouldReturnNewAccessAndRefreshTokens_whenRefreshTokenIsValid() {
        String oldRefreshToken = "old-refresh-token";

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .refreshToken(oldRefreshToken)
                .refreshTokenExpiry(LocalDateTime.now().plusDays(5))
                .build();

        when(userRepository.findByRefreshToken(oldRefreshToken)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(user)).thenReturn("new-jwt-token");

        AuthResponse result = authService.refreshTokens(oldRefreshToken);

        assertThat(result.getToken()).isEqualTo("new-jwt-token");
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRefreshToken()).isNotBlank().isNotEqualTo(oldRefreshToken);
    }

    @Test
    void refreshTokens_shouldRotateRefreshToken_savingNewValueAndFutureExpiry() {
        String oldRefreshToken = "old-refresh-token";

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .refreshToken(oldRefreshToken)
                .refreshTokenExpiry(LocalDateTime.now().plusDays(5))
                .build();

        when(userRepository.findByRefreshToken(oldRefreshToken)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(user)).thenReturn("new-jwt-token");

        authService.refreshTokens(oldRefreshToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRefreshToken()).isNotBlank().isNotEqualTo(oldRefreshToken);
        assertThat(savedUser.getRefreshTokenExpiry()).isAfter(LocalDateTime.now());
    }

    @Test
    void refreshTokens_shouldThrowException_whenRefreshTokenNotFound() {
        when(userRepository.findByRefreshToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshTokens("unknown-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(userRepository).findByRefreshToken("unknown-token");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshTokens_shouldThrowException_whenRefreshTokenIsExpired() {
        String expiredToken = "expired-refresh-token";

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .refreshToken(expiredToken)
                .refreshTokenExpiry(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findByRefreshToken(expiredToken)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshTokens(expiredToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token has expired");

        verify(userRepository).findByRefreshToken(expiredToken);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshTokens_shouldThrowException_whenRefreshTokenExpiryIsNull() {
        String token = "token-without-expiry";

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .refreshToken(token)
                .refreshTokenExpiry(null)
                .build();

        when(userRepository.findByRefreshToken(token)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshTokens(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token has expired");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtService);
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    void logout_shouldClearRefreshToken_whenTokenExists() {
        String refreshToken = "valid-refresh-token";

        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .refreshToken(refreshToken)
                .refreshTokenExpiry(LocalDateTime.now().plusDays(5))
                .build();

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(refreshToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getRefreshToken()).isNull();
        assertThat(savedUser.getRefreshTokenExpiry()).isNull();
    }

    @Test
    void logout_shouldDoNothing_whenRefreshTokenNotFound() {
        when(userRepository.findByRefreshToken("unknown-token")).thenReturn(Optional.empty());

        authService.logout("unknown-token");

        verify(userRepository).findByRefreshToken("unknown-token");
        verify(userRepository, never()).save(any(User.class));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
