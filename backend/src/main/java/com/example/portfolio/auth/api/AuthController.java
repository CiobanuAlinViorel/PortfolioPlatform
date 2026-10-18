package com.example.portfolio.auth.api;

import com.example.portfolio.auth.application.AuthService;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.GoogleAuthRequest;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.RegisterRequest;
import com.example.portfolio.auth.dto.RegisterResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(7);

    private final AuthService authService;

    @Value("${app.auth.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.auth.refresh-cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        RegisterResponse res = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(res);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest req) {
        AuthResponse res = authService.authenticateGoogle(req.getIdToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(res.getRefreshToken()).toString())
                .body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse res = authService.authenticate(req);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(res.getRefreshToken()).toString())
                .body(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse res = authService.refreshTokens(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(res.getRefreshToken()).toString())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    private ResponseCookie buildRefreshCookie(String value) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path("/api/auth")
                .maxAge(REFRESH_COOKIE_MAX_AGE)
                .sameSite(refreshCookieSameSite)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path("/api/auth")
                .maxAge(0)
                .sameSite(refreshCookieSameSite)
                .build();
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
