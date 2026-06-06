package com.example.portfolio.auth.application;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.RegisterRequest;
import com.example.portfolio.auth.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenEmailDoesNotExist() {
        RegisterRequest request = registerRequest("user@test.com", "raw-password");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse result = authService.register(request);

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getToken()).isEqualTo("jwt-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("user@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);

        verify(userRepository).existsByEmail("user@test.com");
        verify(passwordEncoder).encode("raw-password");
        verify(jwtService).generateToken(savedUser);
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
    }

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
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse result = authService.authenticate(request);

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getToken()).isEqualTo("jwt-token");

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder).matches("raw-password", "encoded-password");
        verify(jwtService).generateToken(user);
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