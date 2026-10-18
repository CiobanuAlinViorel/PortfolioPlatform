package com.example.portfolio.auth.api;

import com.example.portfolio.auth.application.AuthService;
import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.dto.*;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.shared.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "app.auth.refresh-cookie.secure=true",
                "app.auth.refresh-cookie.same-site=None"
        }
)
@Import(SecurityConfig.class)

class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // Required by JwtAuthenticationFilter (loaded via SecurityConfig)
    @MockitoBean
    private JwtService jwtService;

    // Required by SecurityConfig.userDetailsService()
    @MockitoBean
    private UserRepository userRepository;

    // Prevents JPA auto-configuration from building a real (empty) metamodel in a @WebMvcTest slice
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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

    private RegisterResponse buildRegisterRes() {
        return new RegisterResponse("user@test.com");
    }


    // ── POST /auth/register ───────────────────────────────────────────────────

    @Test
    void register_shouldReturn201WithEmailAndNoCookie_whenRequestIsValid() throws Exception {
        RegisterRequest req = new RegisterRequest("user@test.com", "password123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(buildRegisterRes());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
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
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")));
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
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")));

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

}
