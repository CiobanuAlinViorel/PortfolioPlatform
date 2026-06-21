package com.example.portfolio.auth.api;

import com.example.portfolio.auth.application.AuthService;
import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.ForgotPasswordRequest;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.RegisterRequest;
import com.example.portfolio.auth.dto.ResetPasswordRequest;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.shared.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Required by JwtAuthenticationFilter (loaded via SecurityConfig)
    @MockBean
    private JwtService jwtService;

    // Required by SecurityConfig.userDetailsService()
    @MockBean
    private UserRepository userRepository;

    private AuthResponse buildAuthResponse(String refreshToken) {
        return AuthResponse.builder()
                .id(1L)
                .email("user@test.com")
                .role(UserRole.USER)
                .token("access-jwt-token")
                .emailVerified(false)
                .refreshToken(refreshToken)
                .build();
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    @Test
    void register_shouldReturn201WithBodyAndCookie_whenRequestIsValid() throws Exception {
        RegisterRequest req = new RegisterRequest("user@test.com", "password123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(buildAuthResponse("refresh-value"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("access-jwt-token"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=refresh-value")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest req = new RegisterRequest("not-an-email", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        RegisterRequest req = new RegisterRequest("user@test.com", "abc");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        RegisterRequest req = new RegisterRequest("user@test.com", "password123");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @Test
    void login_shouldReturn200WithBodyAndCookie_whenCredentialsAreValid() throws Exception {
        LoginRequest req = new LoginRequest("user@test.com", "password123");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(buildAuthResponse("refresh-value"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=refresh-value")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    @Test
    void login_shouldReturn400_whenEmailIsMissing() throws Exception {
        LoginRequest req = new LoginRequest("", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldReturn400_whenCredentialsAreInvalid() throws Exception {
        LoginRequest req = new LoginRequest("user@test.com", "wrong-password");
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    // ── POST /auth/refresh ────────────────────────────────────────────────────

    @Test
    void refresh_shouldReturn200WithNewTokenAndRotatedCookie_whenCookieIsPresent() throws Exception {
        when(authService.refreshTokens("old-refresh")).thenReturn(buildAuthResponse("new-refresh"));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-jwt-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=new-refresh")));
    }

    @Test
    void refresh_shouldReturn401_whenNoCookieIsPresent() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void refresh_shouldReturn400_whenRefreshTokenIsInvalid() throws Exception {
        when(authService.refreshTokens(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "expired-token")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    // ── POST /auth/logout ─────────────────────────────────────────────────────

    @Test
    void logout_shouldReturn200AndClearCookie_whenCookieIsPresent() throws Exception {
        doNothing().when(authService).logout("my-refresh-token");

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "my-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(authService).logout("my-refresh-token");
    }

    @Test
    void logout_shouldReturn200AndClearCookie_whenNoCookieIsPresent() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(authService, never()).logout(anyString());
    }

    // ── GET /auth/verify-email ────────────────────────────────────────────────

    @Test
    void verifyEmail_shouldReturn200_whenTokenIsValid() throws Exception {
        doNothing().when(authService).verifyEmail("valid-token");

        mockMvc.perform(get("/auth/verify-email").param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void verifyEmail_shouldReturn400_whenTokenIsInvalidOrExpired() throws Exception {
        doThrow(new IllegalArgumentException("Verification token has expired"))
                .when(authService).verifyEmail("expired-token");

        mockMvc.perform(get("/auth/verify-email").param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification token has expired"));
    }

    // ── POST /auth/forgot-password ────────────────────────────────────────────

    @Test
    void forgotPassword_shouldReturn200_whenEmailExists() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("user@test.com");
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email is registered, a reset link has been sent"));
    }

    @Test
    void forgotPassword_shouldReturn200_whenEmailDoesNotExist() throws Exception {
        // Service silently does nothing for unknown emails — same response to avoid enumeration
        ForgotPasswordRequest req = new ForgotPasswordRequest("missing@test.com");
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email is registered, a reset link has been sent"));
    }

    @Test
    void forgotPassword_shouldReturn400_whenEmailIsInvalid() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("not-an-email");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verifyNoInteractions(authService);
    }

    // ── POST /auth/reset-password ─────────────────────────────────────────────

    @Test
    void resetPassword_shouldReturn200_whenTokenAndPasswordAreValid() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("valid-token", "newpassword");
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void resetPassword_shouldReturn400_whenTokenIsExpired() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("expired-token", "newpassword");
        doThrow(new IllegalArgumentException("Reset token has expired"))
                .when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reset token has expired"));
    }

    @Test
    void resetPassword_shouldReturn400_whenNewPasswordIsTooShort() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("valid-token", "abc");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPassword").exists());

        verifyNoInteractions(authService);
    }
}
